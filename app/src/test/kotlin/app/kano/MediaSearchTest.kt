package app.kano

import app.kano.data.MediaRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaSearchTest {
    @Test fun filenameWildcardsAreLiteral() {
        assertEquals("%100\\%\\_notes\\\\image%", MediaRepository.searchPattern("100%_notes\\image"))
    }
}
