import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StatusBar } from 'expo-status-bar';
import { PaperProvider } from 'react-native-paper';
import { Platform } from 'react-native';

import HomeScreen from './src/screens/HomeScreen';

// import './src/styles/global.css';

export type RootStackParamList = {
  Home: undefined;
  Login: undefined;
  Dashboard: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
    },
  },
});

export default function App() {
  // Suppress noisy deprecation warnings on web from upstream libs during dev
  useEffect(() => {
    if (Platform.OS === 'web') {
      const origWarn = console.warn;
      console.warn = (...args: any[]) => {
        const msg = args?.[0];
        if (typeof msg === 'string' && (
          msg.includes('props.pointerEvents is deprecated') ||
          msg.includes('"shadow*" style props are deprecated')
        )) {
          return;
        }
        origWarn(...args);
      };
      return () => { console.warn = origWarn; };
    }
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <PaperProvider>
        <NavigationContainer>
          <StatusBar style="auto" />
          <Stack.Navigator initialRouteName="Home">
            <Stack.Screen 
              name="Home" 
              component={HomeScreen} 
              options={{ title: '태권도장 관리 시스템' }}
            />
          </Stack.Navigator>
        </NavigationContainer>
      </PaperProvider>
    </QueryClientProvider>
  );
}
