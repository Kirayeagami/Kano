# AI providers — actual state

AppGraph constructs AiRouter with an empty provider list. Local AI, OpenAI, Gemini,
Perplexity, Google login and Drive are NOT connected. No network permission exists.

The core router validates requested capability and applies text egress policy before a
remote provider call. It does not automatically select a provider, fall back, synthesize
conflicts or implement OAuth. Those are future work, not current functionality.

Future adapters need official API/auth configuration, explicit scopes, securely stored
credentials, disconnect/revocation, response validation, request timeouts and consent.
Consumer ChatGPT/Google subscriptions are not API credentials. No session scraping.
