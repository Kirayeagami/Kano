package app.kano.core

data class FeatureRequirements(val implemented: Boolean, val minApi: Int, val permission: String? = null)
sealed interface Availability {
    data object Available : Availability
    data class Unavailable(val reason: String) : Availability
}
fun FeatureRequirements.availability(api: Int, granted: Set<String>): Availability = when {
    !implemented -> Availability.Unavailable("Planned; not implemented in this build.")
    api < minApi -> Availability.Unavailable("Requires Android API $minApi or later.")
    permission != null && permission !in granted -> Availability.Unavailable("Access required: $permission.")
    else -> Availability.Available
}
