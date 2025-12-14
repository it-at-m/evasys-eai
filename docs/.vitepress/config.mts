import { defineConfig } from "vitepress";
import { withMermaid } from "vitepress-plugin-mermaid";

// https://vitepress.dev/reference/site-config
const vitepressConfig = defineConfig({
  base: "/evasys-eai/",
  title: "evasys EAI",
  description: "Enterprise Application Integration for SAP-PO and evasys",
  head: [
    [
      "link",
      {
        rel: "icon",
        href: `https://assets.muenchen.de/logos/lhm/icon-lhm-muenchen-32.png`,
      },
    ],
  ],
  lastUpdated: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      {
        text: "Documentation",
        items: [
          { text: "Architecture", link: "/architecture" },
          { text: "Configuration", link: "/configuration" },
          { text: "Development", link: "/development" },
          { text: "Deployment", link: "/deployment" },
        ],
      },
    ],
    sidebar: [
      {
        text: "Getting Started",
        items: [
          { text: "Overview", link: "/" },
          { text: "Architecture", link: "/architecture" },
        ],
      },
      {
        text: "Guides",
        items: [
          { text: "Configuration", link: "/configuration" },
          { text: "Development", link: "/development" },
          { text: "Deployment", link: "/deployment" },
        ],
      },
    ],
    socialLinks: [
      { icon: "github", link: "https://github.com/it-at-m/evasys-eai" },
    ],
    editLink: {
      pattern: "https://github.com/it-at-m/evasys-eai/blob/main/docs/:path",
      text: "Edit this page on GitHub",
    },
    footer: {
      message: `<a href="https://opensource.muenchen.de/impress.html">Imprint and Contact</a>`,
    },
    outline: {
      level: "deep",
    },
    search: {
      provider: "local",
    },
  },
  markdown: {
    image: {
      lazyLoading: true,
    },
  },
});

export default withMermaid(vitepressConfig);
