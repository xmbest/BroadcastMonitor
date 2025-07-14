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
        // Hook 系统进程 - 捕获系统级广播
        loadSystem {
            BroadcastHook(this).hookSystemBroadcast()
        }
        
        // Hook 应用进程 - 捕获第三方应用广播 - 只会处理LSPosed中勾选的应用
        loadApp {
            BroadcastHook(this).hookUserBroadcast()
        }
    }

}
