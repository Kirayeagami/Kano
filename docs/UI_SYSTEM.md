# KANO UI system — recovery

Reference direction: the seven user-supplied mobile screenshots (bright surfaces,
coral accents, rounded blocks, clear hierarchy). No copied branding or fabricated content.
Previous Kolpo-based visual guidance is superseded.

Light: warm white surfaces, dark ink, darker coral primary for readable button labels.
Dark: warm charcoal surfaces, warm-white text, restrained coral and muted containers.
Tokens are composition-scoped getters, not mutable global variables shared by previews.
Glass defaults to theme surface at 94% opacity; OFF uses opaque surfaces. No backdrop blur.

Home provides working navigation and factual capability descriptions. Device displays
measured snapshots. Media displays actual index/job states; no semantic category badges.
Care displays only user-entered data. Style clearly reports unavailability.

Six navigation destinations use three columns normally, two at large fonts. Text fields
and long forms scroll; major cards use available width. Facts stack labels and values.
Appearance modes, Glass and Reduce motion persist. Ambient waves are disabled when the
activity is not resumed, the user reduces motion, or Android animations are disabled.
Live validation and remaining gaps are documented in TEST_STATUS.md.
