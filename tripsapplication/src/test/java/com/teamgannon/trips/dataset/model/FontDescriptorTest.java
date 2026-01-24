package com.teamgannon.trips.dataset.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FontDescriptor Tests")
class FontDescriptorTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create with default constructor")
        void shouldCreateWithDefaultConstructor() {
            FontDescriptor font = new FontDescriptor();

            assertNull(font.getName());
            assertEquals(0, font.getSize());
        }

        @Test
        @DisplayName("should create with name and size constructor")
        void shouldCreateWithNameAndSizeConstructor() {
            FontDescriptor font = new FontDescriptor("Arial", 12);

            assertEquals("Arial", font.getName());
            assertEquals(12, font.getSize());
        }

        @Test
        @DisplayName("should create with different font names")
        void shouldCreateWithDifferentFontNames() {
            FontDescriptor arial = new FontDescriptor("Arial", 10);
            FontDescriptor times = new FontDescriptor("Times New Roman", 10);
            FontDescriptor courier = new FontDescriptor("Courier New", 10);

            assertEquals("Arial", arial.getName());
            assertEquals("Times New Roman", times.getName());
            assertEquals("Courier New", courier.getName());
        }

        @Test
        @DisplayName("should create with different sizes")
        void shouldCreateWithDifferentSizes() {
            FontDescriptor small = new FontDescriptor("Arial", 8);
            FontDescriptor medium = new FontDescriptor("Arial", 12);
            FontDescriptor large = new FontDescriptor("Arial", 24);

            assertEquals(8, small.getSize());
            assertEquals(12, medium.getSize());
            assertEquals(24, large.getSize());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get name")
        void shouldSetAndGetName() {
            FontDescriptor font = new FontDescriptor();
            font.setName("Helvetica");

            assertEquals("Helvetica", font.getName());
        }

        @Test
        @DisplayName("should set and get size")
        void shouldSetAndGetSize() {
            FontDescriptor font = new FontDescriptor();
            font.setSize(16);

            assertEquals(16, font.getSize());
        }

        @Test
        @DisplayName("should allow changing name after construction")
        void shouldAllowChangingNameAfterConstruction() {
            FontDescriptor font = new FontDescriptor("Arial", 12);
            font.setName("Verdana");

            assertEquals("Verdana", font.getName());
        }

        @Test
        @DisplayName("should allow changing size after construction")
        void shouldAllowChangingSizeAfterConstruction() {
            FontDescriptor font = new FontDescriptor("Arial", 12);
            font.setSize(20);

            assertEquals(20, font.getSize());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle empty font name")
        void shouldHandleEmptyFontName() {
            FontDescriptor font = new FontDescriptor("", 12);

            assertEquals("", font.getName());
        }

        @Test
        @DisplayName("should handle zero size")
        void shouldHandleZeroSize() {
            FontDescriptor font = new FontDescriptor("Arial", 0);

            assertEquals(0, font.getSize());
        }

        @Test
        @DisplayName("should handle very large size")
        void shouldHandleVeryLargeSize() {
            FontDescriptor font = new FontDescriptor("Arial", 1000);

            assertEquals(1000, font.getSize());
        }

        @Test
        @DisplayName("should handle font name with spaces")
        void shouldHandleFontNameWithSpaces() {
            FontDescriptor font = new FontDescriptor("Times New Roman", 12);

            assertEquals("Times New Roman", font.getName());
        }

        @Test
        @DisplayName("should handle font name with special characters")
        void shouldHandleFontNameWithSpecialCharacters() {
            FontDescriptor font = new FontDescriptor("Font-Name_v2.0", 12);

            assertEquals("Font-Name_v2.0", font.getName());
        }
    }

    @Nested
    @DisplayName("Common Font Configurations Tests")
    class CommonFontConfigurationsTests {

        @Test
        @DisplayName("should create typical star label font")
        void shouldCreateTypicalStarLabelFont() {
            FontDescriptor font = new FontDescriptor("Arial", 8);

            assertEquals("Arial", font.getName());
            assertEquals(8, font.getSize());
        }

        @Test
        @DisplayName("should create typical polity label font")
        void shouldCreateTypicalPolityLabelFont() {
            FontDescriptor font = new FontDescriptor("Arial", 10);

            assertEquals("Arial", font.getName());
            assertEquals(10, font.getSize());
        }

        @Test
        @DisplayName("should create typical link label font")
        void shouldCreateTypicalLinkLabelFont() {
            FontDescriptor font = new FontDescriptor("Arial", 10);

            assertEquals("Arial", font.getName());
            assertEquals(10, font.getSize());
        }

        @Test
        @DisplayName("should create heading font")
        void shouldCreateHeadingFont() {
            FontDescriptor font = new FontDescriptor("Helvetica", 18);

            assertEquals("Helvetica", font.getName());
            assertEquals(18, font.getSize());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal for same name and size")
        void shouldBeEqualForSameNameAndSize() {
            FontDescriptor font1 = new FontDescriptor("Arial", 12);
            FontDescriptor font2 = new FontDescriptor("Arial", 12);

            assertEquals(font1, font2);
        }

        @Test
        @DisplayName("should not be equal for different names")
        void shouldNotBeEqualForDifferentNames() {
            FontDescriptor font1 = new FontDescriptor("Arial", 12);
            FontDescriptor font2 = new FontDescriptor("Verdana", 12);

            assertNotEquals(font1, font2);
        }

        @Test
        @DisplayName("should not be equal for different sizes")
        void shouldNotBeEqualForDifferentSizes() {
            FontDescriptor font1 = new FontDescriptor("Arial", 12);
            FontDescriptor font2 = new FontDescriptor("Arial", 14);

            assertNotEquals(font1, font2);
        }

        @Test
        @DisplayName("should have same hashCode for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            FontDescriptor font1 = new FontDescriptor("Arial", 12);
            FontDescriptor font2 = new FontDescriptor("Arial", 12);

            assertEquals(font1.hashCode(), font2.hashCode());
        }
    }
}
