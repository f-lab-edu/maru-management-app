import React from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/api'
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { RootStackParamList } from '../../App';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

export default function HomeScreen({ navigation }: Props) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['mobile-test-message'],
    queryFn: async () => {
      const res = await api.get<string>('/test', { responseType: 'text' })
      return res.data
    },
  })

  return (
    <View className="flex-1 justify-center items-center bg-gray-50 p-6">
      <View className="items-center gap-3">
        <Text className="text-2xl font-bold text-gray-800">마루 태권도장 관리 시스템</Text>
        {isLoading && (
          <View className="flex-row items-center gap-2">
            <ActivityIndicator />
            <Text className="text-gray-600">불러오는 중...</Text>
          </View>
        )}
        {error && <Text className="text-red-600">불러오기에 실패했습니다.</Text>}
        {!!data && <Text className="text-gray-900">{data}</Text>}
      </View>
    </View>
  );
}
