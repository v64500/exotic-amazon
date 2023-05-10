package ai.platon.exotic.common

import ai.platon.pulsar.common.ResourceLoader
import ai.platon.pulsar.common.getLogger
import java.nio.file.*

class ResourceWalker: AutoCloseable {
    companion object {
        val SPRING_PACKEDR_ESOURCE_PREFIX = "BOOT-INF/classes/"
    }

    private val logger = getLogger(this)
    // When the jar is packed using spring-boot, the resources are put into directory BOOT-INF/classes
    var resourcePrefix: String = ""

    private var fileSystem: FileSystem? = null

    init {
        val uri = ResourceLoader.getResource(SPRING_PACKEDR_ESOURCE_PREFIX)?.toURI()
        if (uri != null && uri.scheme == "jar") {
            resourcePrefix = SPRING_PACKEDR_ESOURCE_PREFIX
        }
    }

    fun getPath(resource: String): Path? {
        val uri = ResourceLoader.getResource(resource)?.toURI() ?: return null

        try {
            return if (uri.scheme == "jar") {
                val env: MutableMap<String, String> = HashMap()
                fileSystem = FileSystems.newFileSystem(uri, env)
                fileSystem?.getPath("$resourcePrefix$resource")
            } else {
                Paths.get(uri)
            }
        } catch (t: Throwable) {
            logger.warn("Unexpected failure get path | $resource", t)
        } finally {
            // Do not close
            // fileSystem?.close()
        }

        return null
    }

    fun list(resourceBase: String): Set<Path> {
        val paths = mutableSetOf<Path>()
        walk(resourceBase, 1) {
            if (!it.toString().endsWith(resourceBase)) {
                paths.add(it)
            }
        }
        return paths
    }

    fun walk(resourceBase: String, maxDepth: Int, visitor: (Path) -> Unit) {
        val path = getPath(resourceBase)

        val walk = Files.walk(path, maxDepth)
        val it = walk.iterator()
        while (it.hasNext()) {
            try {
                visitor(it.next())
            } catch (e: Exception) {
                logger.warn("Failed to visit path | $path", e)
            }
        }
    }

    override fun close() {
        fileSystem?.close()
    }
}
