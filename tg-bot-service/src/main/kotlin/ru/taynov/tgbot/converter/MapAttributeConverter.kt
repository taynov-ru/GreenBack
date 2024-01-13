import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class MapAttributeConverter : AttributeConverter<Map<String, String>, String> {

    private val json = Json { encodeDefaults = true }

    override fun convertToDatabaseColumn(attribute: Map<String, String>?): String? {
        return attribute?.let {
            try {
                json.encodeToString(it)
            } catch (e: Exception) {
                throw RuntimeException("Error converting map to JSON string", e)
            }
        }
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, String>? {
        return dbData?.let {
            try {
                json.decodeFromString<Map<String, String>>(it)
            } catch (e: Exception) {
                throw RuntimeException("Error converting JSON string to map", e)
            }
        }
    }
}
