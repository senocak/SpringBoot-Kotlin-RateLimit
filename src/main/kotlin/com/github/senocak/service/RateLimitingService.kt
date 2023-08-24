package com.github.senocak.service

import com.github.senocak.domain.Package
import com.github.senocak.exception.ServerException
import com.github.senocak.repository.PackageRepository
import com.github.senocak.util.OmaErrorMessageType
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import net.jodah.expiringmap.ExpiringMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class RateLimitingService(private val packageRepository: PackageRepository){
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    private val cache: ExpiringMap<String, RateLimitInfo> = ExpiringMap.builder()
        .variableExpiration()
        .asyncExpirationListener { key: String, rateLimitInfo: RateLimitInfo ->
            log.info("Key Removed $key: ${rateLimitInfo.key} at ${rateLimitInfo.resetTime}") }
        .build<String, RateLimitInfo>()

    //private val bucketCache: MutableMap<String, Bucket> = ConcurrentHashMap()

    //fun resolveBucket(userId: String): Bucket =
    //    bucketCache.computeIfAbsent("") { uId ->
    //        val plan: Package = packageRepository.findById(uId)
    //            .orElseThrow {
    //                ServerException(
    //                    omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
    //                    variables = arrayOf(),
    //                    statusCode = HttpStatus.NOT_FOUND
    //                )
    //            }
    //        val limitPerHour: Long = plan.limitPer
    //        Bucket4j.builder()
    //            .addLimit(Bandwidth.classic(limitPerHour, Refill.intervally(limitPerHour, Duration.ofHours(1))))
    //            .build()
    //    }

    fun getRateLimitResponse(userId: String, packageId: String): RateLimitInfo {
        val key = "$userId+$packageId"
        val plan: Package = packageRepository.findById(packageId)
            .orElseThrow {
                ServerException(
                    omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
                    variables = arrayOf(),
                    statusCode = HttpStatus.NOT_FOUND
                )
            }
        if (cache.containsKey(key = key)) {
            val rateLimitInfo: RateLimitInfo = cache[key] ?: throw Exception("$key not found...")
            rateLimitInfo.reduceHits(hit = 1)
            rateLimitInfo.resetTime = cache.getExpectedExpiration(key)
            return rateLimitInfo
        }
        return RateLimitInfo()
            .also {rli: RateLimitInfo ->
                rli.key = key
                rli.limit = plan.limitPer
                rli.duration = 1
                rli.timeUnit = TimeUnit.valueOf(value = plan.type)
                cache.put(key, rli, rli.duration, rli.timeUnit)
                rli.resetTime = cache.getExpectedExpiration(key)
            }
    }
}

class RateLimitInfo{
    var limit: Long = 1
    var key: String? = null
    var timeUnit: TimeUnit = TimeUnit.MINUTES
    var duration: Long = 1
    val counter: AtomicLong = AtomicLong(limit)
    var resetTime: Long = System.currentTimeMillis()

    fun reduceHits(hit: Long): Long = counter.addAndGet(hit)

    override fun toString(): String =
        "RateLimitInfo(limit=$limit, key=$key, timeUnit=$timeUnit, duration=$duration, counter=$counter, resetTime=$resetTime)"
}