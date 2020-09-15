/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.caches.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.analyzer.PlatformAnalysisParameters
import org.jetbrains.kotlin.analyzer.ResolverForModuleFactory
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltIns
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.idea.caches.project.KotlinReflectLibraryInfo
import org.jetbrains.kotlin.idea.caches.project.KotlinStdlibInfo
import org.jetbrains.kotlin.idea.caches.project.LibraryInfo
import org.jetbrains.kotlin.idea.caches.project.SdkInfo
import org.jetbrains.kotlin.idea.caches.resolve.BuiltInsCacheKey
import org.jetbrains.kotlin.idea.caches.resolve.isKotlinReflect
import org.jetbrains.kotlin.idea.caches.resolve.isKotlinStdlib
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.impl.JvmIdePlatformKind
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.resolve.TargetEnvironment
import org.jetbrains.kotlin.resolve.jvm.JvmPlatformParameters
import org.jetbrains.kotlin.resolve.jvm.JvmResolverForModuleFactory

class JvmPlatformKindResolution : IdePlatformKindResolution {
    override fun isLibraryFileForPlatform(virtualFile: VirtualFile): Boolean {
        return false // TODO: No library kind for JVM
    }

    override fun createResolverForModuleFactory(
        settings: PlatformAnalysisParameters,
        environment: TargetEnvironment,
        platform: TargetPlatform
    ): ResolverForModuleFactory {
        return JvmResolverForModuleFactory(settings as JvmPlatformParameters, environment, platform)
    }

    override val libraryKind: PersistentLibraryKind<*>?
        get() = null

    override fun createLibraryInfo(project: Project, library: Library): List<LibraryInfo> = when {
        library.isKotlinStdlib -> listOf(KotlinStdlibInfo(project, library, JvmPlatforms.defaultJvmPlatform))
        library.isKotlinReflect -> listOf(KotlinReflectLibraryInfo(project, library, JvmPlatforms.defaultJvmPlatform))
        else -> listOf(JvmLibraryInfo(project, library))
    }

    override val kind get() = JvmIdePlatformKind

    override fun getKeyForBuiltIns(moduleInfo: ModuleInfo, sdkInfo: SdkInfo?): BuiltInsCacheKey {
        return if (sdkInfo != null && moduleInfo !is SdkInfo) CacheKeyBySdk(sdkInfo.sdk) else BuiltInsCacheKey.DefaultBuiltInsKey
    }

    override fun createBuiltIns(moduleInfo: ModuleInfo, projectContext: ProjectContext, sdkDependency: SdkInfo?): KotlinBuiltIns {
        return if (sdkDependency != null && moduleInfo !is SdkInfo)
            JvmBuiltIns(projectContext.storageManager, JvmBuiltIns.Kind.FROM_DEPENDENCIES)
//            JvmBuiltIns(projectContext.storageManager, JvmBuiltIns.Kind.FROM_CLASS_LOADER)
        else
            DefaultBuiltIns.Instance
    }

    data class CacheKeyBySdk(val sdk: Sdk) : BuiltInsCacheKey
}

class JvmLibraryInfo(project: Project, library: Library) : LibraryInfo(project, library) {
    override val platform: TargetPlatform
        get() = JvmPlatforms.defaultJvmPlatform
}
