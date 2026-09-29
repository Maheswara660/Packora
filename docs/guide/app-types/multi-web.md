# Multi-Web Hub Architecture

The **Multi-Web** architecture (`PackoraAppType.MULTI_WEB`) aggregates multiple related web destinations into a single cohesive Android application featuring an interactive native pill tab bar.

---

## Interactive Native Tab Navigation

When an app is compiled with `MULTI_WEB` mode:
- **Primary Home Tab**: The main URL provided in Build Studio serves as the primary home tab.
- **Secondary URLs**: Additional URLs specified in the dedicated Multi-Web configuration card are added as sequential navigation tabs.
- **Native Pill Bar**: Packora injects a sleek, dark pill tab bar at the top or bottom of the screen. Users can tap between tabs instantaneously with individual WebView state preservation.
- **Independent Session Isolation**: Cookies and session storage can be shared or partitioned depending on destination domains.

---

## Best For

- Multi-service portals (e.g. main application + documentation portal + support forum).
- Company dashboards consolidating multiple internal web tools.
- Multi-feed social or news aggregators.
- Cross-platform management suites.
