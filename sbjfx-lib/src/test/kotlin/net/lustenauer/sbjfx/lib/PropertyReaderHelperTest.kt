package net.lustenauer.sbjfx.lib

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.core.env.Environment

internal class PropertyReaderHelperTest {

    private lateinit var envArrayMock: Environment
    private lateinit var envSingleEntryMock: Environment

    @BeforeEach
    fun setUp() {
        envArrayMock = Mockito.mock(Environment::class.java)
        envSingleEntryMock = Mockito.mock(Environment::class.java)
        // This is what Spring environment returns
        // When we defined an array in of appicons: ('- entry_1' ... in the
        // yaml):
        Mockito.`when`(envArrayMock.getProperty("entry")).thenReturn(null)
        Mockito.`when`(envArrayMock.getProperty("entry[0]")).thenReturn("entry_0")
        Mockito.`when`(envArrayMock.getProperty("entry[1]")).thenReturn("entry_1")
        Mockito.`when`(envArrayMock.getProperty("entry[2]")).thenReturn("entry_2")

        // When there is a single entry:
        Mockito.`when`(envSingleEntryMock.getProperty("entry")).thenReturn("entry")
        Mockito.`when`(envSingleEntryMock.getProperty("entry[0]")).thenReturn(null)
        Mockito.`when`(
            envSingleEntryMock.getProperty(
                ArgumentMatchers.eq("stringentry"), ArgumentMatchers.eq(
                    String::class.java
                )
            )
        ).thenReturn("entry")
    }

    @Test
    @DisplayName("Single value")
    @Throws(Exception::class)
    fun singleValueTest() {
        val list = PropertyReaderHelper[envSingleEntryMock, "entry"]
        assertThat(list).containsExactlyInAnyOrder("entry")
    }

    @Test
    @DisplayName("Multiple values")
    fun multipleValuesTest() {
        val list = PropertyReaderHelper[envArrayMock, "entry"]
        assertThat(list).containsExactlyInAnyOrder("entry_0", "entry_1", "entry_2")
    }

    @Test
    @DisplayName("Set if existing key is present ")
    fun setIfExistingKeyIsPresentTest() {
        val testObject = TestObject()
        PropertyReaderHelper.setIfPresent(
            env = envSingleEntryMock,
            key = "stringentry",
            type = String::class.java
        ) { theEntryValue: String -> testObject.stringEntry = theEntryValue }
        assertThat(testObject.stringEntry).isEqualTo("entry")
    }

    @Test
    @DisplayName("Set if existing key is not present ")
    fun setIfExistingKeyIsNotPresentTest() {
        val testObject = TestObject()
        PropertyReaderHelper.setIfPresent(
            env = envSingleEntryMock,
            key = "no_entry",
            type = String::class.java
        ) { theEntryValue: String ->
            testObject.stringEntry = theEntryValue
        }
        assertThat(testObject.stringEntry).isEqualTo("UNSET")
    }

    @Test
    @DisplayName("Determine file path from package name")
    fun determineFilePathFromPackageNameTest() {
        val path = PropertyReaderHelper.determineFilePathFromPackageName(javaClass)
        assertEquals("/net/lustenauer/sbjfx/lib/", path)
    }

    internal class TestObject {
        var stringEntry: String = "UNSET"
    }
}
