package com.github.senocak.controller

import com.github.senocak.domain.Package
import com.github.senocak.domain.dto.user.PackageDto
import com.github.senocak.repository.PackageRepository
import com.github.senocak.service.Extensions.convertEntityToDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(SharedController.URL)
class SharedController(
    private val packageRepository: PackageRepository
): BaseController() {
    @GetMapping("/packages")
    fun packages(): List<PackageDto> =
        packageRepository.findAll().map { p: Package -> p.convertEntityToDto() }

    companion object {
        const val URL = "/api/v1/shared"
    }
}
