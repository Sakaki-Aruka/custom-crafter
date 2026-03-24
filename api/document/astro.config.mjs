// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	integrations: [
		starlight({
			title: 'CustomCrafterAPI documents',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/Sakaki-Aruka/custom-crafter' }],
			defaultLocale: 'en',
			locales: {
                en: {
                    label: 'English',
                },
                ja: {
                    label: '日本語',
                    lang: 'ja-JP'
                }
            },
			sidebar: [
                {
                    label: 'Getting started',
                    translations: { ja: 'はじめに' },
                    items: ['getting-started'],
                },
                {
                    label: 'Recipe',
                    translations: { ja: 'レシピ' },
                    items: ['recipe/recipe', 'recipe/matter', 'recipe/result']
                }
			],
		}),
	],
});
