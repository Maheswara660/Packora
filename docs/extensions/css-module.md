# CSS Plugins

A CSS plugin is a pure style override — theming, restyling, or a dark mode for a site. It uses the same `plugin.json` manifest as an [HCJ plugin](/extensions/js-module), but the substance is the stylesheet.

::: tip Preferred: `hcj.addStyle(css)`
Page styles are normally authored inside `main.js` via `hcj.addStyle(css)` — no separate file needed, and the editor only exposes `main.js` / `panel.html`. A bare `style.css` file still loads for compatibility (older or migrated packages), as described below.
:::

## File layout

```
my-theme/
├── plugin.json    # required
├── main.js        # required — can be a near-empty stub
├── style.css      # legacy optional — prefer hcj.addStyle in main.js
└── icon.png       # optional
```

::: info A `main.js` is still required
Even a pure CSS plugin needs a `main.js` (it can be a minimal stub). Set `runAt` to `document_start` so your styles apply as early as possible and avoid a flash of unstyled content.
:::

## `plugin.json`

```json
{
  "id": "dark-reader-lite",
  "name": "Dark Reader Lite",
  "description": "A simple dark theme",
  "icon": "dark_mode",
  "runAt": "document_start",
  "matches": ["*://news.ycombinator.com/*"],
  "permissions": []
}
```

CSS injection needs no permission — it's part of the package.

## How CSS is injected

`style.css` is injected as a `<style id="hcj-css-<id>">` element at document-start, before the page paints and before `main.js` runs. It is idempotent per plugin per document — re-navigations won't stack duplicates.

## Example `style.css`

```css
:root {
  color-scheme: dark;
}
body {
  background: #111 !important;
  color: #ddd !important;
}
a {
  color: #60a5fa !important;
}
```

See the built-in `builtin-dark-mode` package under [`app/src/main/assets/plugins/`](https://github.com/Maheswara660/Packora/tree/main/app/src/main/assets/plugins) for a working dark-mode plugin (it injects its styles from `main.js`).
