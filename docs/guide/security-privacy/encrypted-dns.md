# Encrypted DNS-over-HTTPS (DoH)

Packora features native **DNS-over-HTTPS (DoH)** network resolution implemented via an isolated in-app OkHttp DoH engine (`PackoraDnsManager`). This prevents Internet Service Providers (ISPs), mobile network carriers, and local Wi-Fi eavesdroppers from snooping on your domain lookups or tampering with DNS responses.

---

## 9 Privacy Resolvers

Packora provides instant 1-tap selection among 9 privacy-respecting resolvers:

| Provider | Primary URL / Endpoint | Specialty |
| :--- | :--- | :--- |
| **Cloudflare DNS** | `https://cloudflare-dns.com/dns-query` | Ultra-fast global Anycast resolution (`1.1.1.1`). |
| **Google Public DNS** | `https://dns.google/dns-query` | Resilient worldwide infrastructure (`8.8.8.8`). |
| **AdGuard DNS** | `https://dns.adguard-dns.com/dns-query` | Network-level ad and tracking domain blocking. |
| **NextDNS** | `https://dns.nextdns.io` | Cloud privacy and analytics protection. |
| **CleanBrowsing** | `https://doh.cleanbrowsing.org/doh/security-filter/` | Blocks phishing, malware, and malicious domains. |
| **Quad9 DNS** | `https://dns.quad9.net/dns-query` | Swiss non-profit privacy & threat defense (`9.9.9.9`). |
| **Mullvad DoH** | `https://doh.mullvad.net/dns-query` | Audited zero-logging Swedish privacy resolver. |
| **System Default** | `[OS Default Provider]` | Standard Wi-Fi / cellular network DNS. |
| **Custom Endpoint** | `https://your-custom-doh.com/dns-query` | User-defined RFC 8484 HTTPS endpoint. |

---

## Strict DoH Mode & Fallback Control

- **Strict Mode Enabled**: Forces all DNS lookups to succeed exclusively through the encrypted HTTPS transport. If the DoH server is unreachable, network queries fail rather than leaking plain-text UDP queries to your local ISP.
- **Fallback Mode**: If encrypted transport encounters network timeouts, gracefully falls back to system DNS to prevent connectivity interruptions.
