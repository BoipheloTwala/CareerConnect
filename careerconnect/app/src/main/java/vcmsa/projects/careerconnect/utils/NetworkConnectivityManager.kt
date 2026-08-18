//CODE ATTRIBUTION
//01
//ConnectivityManager
//Adapted from: Android Developers. (2025). ConnectivityManager. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/net/ConnectivityManager
//Date Accessed: 15 November 2025

//02
//NetworkCallback
//Adapted from: Android Developers. (2025). NetworkCallback. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Manager for monitoring network connectivity status
 * Provides real-time network status updates via Flow
 */
class NetworkConnectivityManager(context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Check if device is currently connected to internet
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Get network type (WiFi, Cellular, etc.)
     */
    fun getNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }
    }
    
    /**
     * Observe network connectivity changes as Flow
     * Returns true when connected, false when disconnected
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()
            
            override fun onAvailable(network: Network) {
                networks.add(network)
                trySend(networks.isNotEmpty())
            }
            
            override fun onLost(network: Network) {
                networks.remove(network)
                trySend(networks.isNotEmpty())
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                
                if (hasInternet) {
                    networks.add(network)
                } else {
                    networks.remove(network)
                }
                
                trySend(networks.isNotEmpty())
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Send current state immediately
        trySend(isConnected())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Observe network type changes
     */
    fun observeNetworkType(): Flow<NetworkType> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getNetworkType())
            }
            
            override fun onLost(network: Network) {
                trySend(getNetworkType())
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(getNetworkType())
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Send current state immediately
        trySend(getNetworkType())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Network type enum
     */
    enum class NetworkType {
        WIFI,
        CELLULAR,
        ETHERNET,
        OTHER,
        NONE
    }
}

