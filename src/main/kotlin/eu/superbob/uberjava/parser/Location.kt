package eu.superbob.uberjava.parser

import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

interface Location {
    fun open(): InputStream

    fun exists(): Boolean

    companion object {
        fun direct(path: Path): Location {
            return DirectLocation(path)
        }

        fun zip(archivePath: Path, path: Path): Location {
            return ZipEntryLocation(archivePath, path)
        }
    }
}

internal class DirectLocation(private val mainLocation: Path) : Location {
    override fun open(): InputStream {
        return Files.newInputStream(mainLocation)
    }

    override fun exists(): Boolean {
        return Files.exists(mainLocation)
    }
}

internal class ZipEntryLocation(private val archiveLocation: Path, private val mainLocation: Path) : Location {
    override fun open(): InputStream {
        val fileSystem = FileSystems.newFileSystem(archiveLocation, null)
        val zipEntryInputStream = Files.newInputStream(fileSystem.getPath(mainLocation.toString()))
        return object : InputStream() {
            override fun read(b: ByteArray): Int {
                return zipEntryInputStream.read(b)
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                return zipEntryInputStream.read(b, off, len)
            }

            override fun skip(n: Long): Long {
                return zipEntryInputStream.skip(n)
            }

            override fun available(): Int {
                return zipEntryInputStream.available()
            }

            override fun close() {
                zipEntryInputStream.close()
                fileSystem.close()
            }

            override fun mark(readlimit: Int) {
                zipEntryInputStream.mark(readlimit)
            }

            override fun reset() {
                zipEntryInputStream.reset()
            }

            override fun markSupported(): Boolean {
                return zipEntryInputStream.markSupported()
            }

            override fun read(): Int {
                return zipEntryInputStream.read()
            }
        }
    }

    override fun exists(): Boolean =
            FileSystems.newFileSystem(archiveLocation, null)
                    .use { Files.exists(it.getPath(mainLocation.toString())) }
}
