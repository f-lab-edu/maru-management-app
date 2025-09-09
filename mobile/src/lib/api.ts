import axios from 'axios'
import Constants from 'expo-constants'
import { Platform } from 'react-native'

function resolveBaseURL(): string {
  const fromEnv = process.env.EXPO_PUBLIC_API_URL
  if (fromEnv && fromEnv.trim().length > 0) return fromEnv

  if (Platform.OS !== 'web' && __DEV__) {
    const hostUri = (Constants as any)?.expoConfig?.hostUri as string | undefined
    const host = hostUri?.split(':')?.[0]
    if (host) return `http://${host}:8080`
  }

  return 'http://localhost:8080'
}

export const api = axios.create({
  baseURL: resolveBaseURL(),
  headers: {
    Accept: 'application/json, text/plain, */*',
  },
})
