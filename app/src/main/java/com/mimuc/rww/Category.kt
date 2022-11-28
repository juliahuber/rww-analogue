import com.mimuc.rww.R

enum class Category(
    var value: String,
    var iconLarge: Int = 0,
    var iconSmall: Int = 0,
    var img: Int = 0)
{
    RELAXING("Relaxing", R.drawable.ic_icon_relax, R.drawable.ic_small_relax, R.drawable.ic_img_relax),
    MENTAL("Mental", R.drawable.ic_icon_mental, R.drawable.ic_small_mental, R.drawable.ic_img_mental),
    PHYSICAL("Physical", R.drawable.ic_icon_physical, R.drawable.ic_small_physical, R.drawable.ic_img_physical),
    SOCIAL("Social", R.drawable.ic_icon_social, R.drawable.ic_small_social, R.drawable.ic_img_social),
    ORGANIZING("Organizing", R.drawable.ic_icon_organizing, R.drawable.ic_small_organizing, R.drawable.ic_img_organizing),
    MISC("Misc", R.drawable.ic_icon_misc, R.drawable.ic_small_misc, R.drawable.ic_img_misc),

    PERSONALIZED("Personalized"),
    RANDOM("Random");

    companion object {
        fun getCategoryByName(value: String): Category? {
            var category: Category? = null
            when (value.lowercase()) {
                "relaxing" -> category = RELAXING
                "mental" -> category = MENTAL
                "physical" -> category = PHYSICAL
                "social" -> category = SOCIAL
                "organizing" -> category = ORGANIZING
                "misc" -> category = MISC
                else -> {}
            }
            return category
        }

        fun getNameByCategory(cat: Category): String {
            var name = ""
            when (cat) {
                RELAXING -> name = "Relaxing"
                MENTAL -> name = "Mental"
                PHYSICAL -> name = "Physical"
                SOCIAL -> name = "Social"
                ORGANIZING -> name = "Organizing"
                MISC -> name = "Misc"
            }
            return name
        }
    }
}