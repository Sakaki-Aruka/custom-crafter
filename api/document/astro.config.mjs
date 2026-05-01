// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

const base = process.env.DEPLOY_BASE ?? '';
const site = (process.env.DEPLOY_SITE ?? 'http://localhost:4321') + base;

// https://astro.build/config
export default defineConfig({
	site,
	base,
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
                    label: 'CustomCrafterAPI',
                    items: ['api/customcrafter-api', 'api/search'],
                },
                {
                    label: 'Recipe',
                    translations: { ja: 'レシピ' },
                    items: ['recipe/recipe', 'recipe/matter', 'recipe/result', 'recipe/predicate'],
                },
                {
                    label: 'Extra',
                    translations: { ja: '追加要素' },
                    items: ['extra/craftui-designer'],
                },
                {
                    label: 'Events',
                    translations: { ja: 'イベント' },
                    items: ['events'],
                },
                {
                    label: 'Objects',
                    translations: { ja: 'オブジェクト' },
                    items: [
                        'objects/async-context',
                        'objects/craft-view',
                        'objects/mapped-relation',
                    ],
                },
                {
                    label: 'Utilities',
                    translations: { ja: 'ユーティリティ' },
                    items: [
                        'util/converter',
                        'util/inventory-util',
                    ],
                },
                {
                    label: 'Implementations',
                    translations: { ja: '実装クラス' },
                    items: [
                        {
                            label: 'Matter',
                            translations: { ja: 'Matter' },
                            items: [
                                'impl/matter/cmatter-impl',
                                'impl/matter/enchant',
                                'impl/matter/potion',
                            ],
                        },
                        {
                            label: 'Recipe',
                            translations: { ja: 'Recipe' },
                            items: [
                                'impl/recipe/crecipe-impl',
                                'impl/recipe/group-recipe',
                                'impl/recipe/cvanilla-recipe',
                                'impl/recipe/adjacent-recipe',
                            ],
                        },
                    ],
                },
			],
		}),
	],
});
