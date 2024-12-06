package com.github.sakakiaruka.customcrafter.customcrafter.api.interfaces.matter

import com.github.sakakiaruka.customcrafter.customcrafter.api.`object`.matter.enchant.CEnchantComponent

interface CEnchantmentStoreMatter: CMatter {
    val storedEnchantComponents: Set<CEnchantComponent>
}