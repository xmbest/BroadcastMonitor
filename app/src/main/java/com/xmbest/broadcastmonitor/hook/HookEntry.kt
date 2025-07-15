package com.xmbest.broadcastmonitor.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.xmbest.broadcastmonitor.BuildConfig

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        isDebug = BuildConfig.DEBUG
    }

    override fun onHook() = encase {
        // Hook system process - capture system-level broadcasts
        loadSystem {
            BroadcastHook(this).hookSystemBroadcast()
        }
        
        // Hook application process - capture third-party app broadcasts - only processes apps checked in LSPosed
        loadApp {
            BroadcastHook(this).hookUserBroadcast()
        }
    }

}
