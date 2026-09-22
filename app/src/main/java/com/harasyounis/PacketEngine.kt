package com.harasyounis

import android.content.Context
import java.nio.ByteBuffer

/**
 * Packet-engine boundary for حارس يونس.
 *
 * A TUN interface carries raw IP packets. A production implementation must:
 *  1. parse IPv4/IPv6;
 *  2. identify TCP/UDP flows;
 *  3. apply the per-app/per-network rule;
 *  4. proxy TCP/UDP to the real underlying network;
 *  5. translate replies back into the TUN interface;
 *  6. handle DNS and connection lifecycle.
 *
 * Keeping this boundary separate prevents the UI/rules code from being coupled
 * to the networking implementation.
 */
interface PacketEngine {
    fun start()
    fun stop()
}

class RuleEngine(private val context: Context) {
    private val prefs = context.getSharedPreferences("rules", Context.MODE_PRIVATE)

    fun isBlocked(packageName: String, mode: NetworkMode): Boolean {
        return when (mode) {
            NetworkMode.WIFI -> prefs.getBoolean("${packageName}_wifi", false)
            NetworkMode.MOBILE -> prefs.getBoolean("${packageName}_mobile", false)
            NetworkMode.OTHER -> false
        }
    }
}

/**
 * Placeholder until the TCP/UDP forwarding backend is included.
 * It deliberately does not claim to provide transparent internet forwarding.
 */
class TunPacketEngine(private val context: Context) : PacketEngine {
    private var running = false
    override fun start() { running = true }
    override fun stop() { running = false }
}
