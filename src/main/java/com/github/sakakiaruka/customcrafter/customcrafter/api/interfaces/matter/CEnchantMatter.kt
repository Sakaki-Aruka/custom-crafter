package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent

interface CEnchantMatter: CMatter {
    val enchantComponents: Set<CEnchantComponent>
}