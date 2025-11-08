import React, { useState, useEffect } from "react";
import { View, Text, ScrollView, TouchableOpacity, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  Heart,
  Phone,
  MapPin,
  Clock,
  Users,
  Shield,
} from "lucide-react-native";
import { router } from "expo-router";

export default function Dashboard() {
  const insets = useSafeAreaInsets();
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const handleEmergencyPress = () => {
    Alert.alert(
      "Emergency Alert",
      "Are you sure you need emergency assistance? This will notify nearby volunteers immediately.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Yes, Get Help",
          style: "destructive",
          onPress: () => router.push("/(tabs)/emergency"),
        },
      ],
    );
  };

  const quickActions = [
    {
      title: "Call Volunteer",
      subtitle: "Get help now",
      icon: Phone,
      color: "#E8F5E8",
      iconColor: "#27AE60",
      onPress: () => router.push("/(tabs)/volunteers"),
    },
    {
      title: "Health Records",
      subtitle: "View your info",
      icon: Heart,
      color: "#FFF0F0",
      iconColor: "#E74C3C",
      onPress: () => router.push("/(tabs)/health"),
    },
    {
      title: "Find Nearby Help",
      subtitle: "Volunteers near you",
      icon: MapPin,
      color: "#F0F8FF",
      iconColor: "#3498DB",
      onPress: () => router.push("/(tabs)/volunteers"),
    },
    {
      title: "Safety Check",
      subtitle: "Daily wellness",
      icon: Shield,
      color: "#FFF8E1",
      iconColor: "#F39C12",
      onPress: () => Alert.alert("Safety Check", "How are you feeling today?"),
    },
  ];

  return (
    <View style={{ flex: 1, backgroundColor: "#FFF8F0" }}>
      <StatusBar style="dark" />

      {/* Header */}
      <View
        style={{
          paddingTop: insets.top + 20,
          paddingHorizontal: 20,
          paddingBottom: 20,
          backgroundColor: "#F4D03F",
          borderBottomLeftRadius: 25,
          borderBottomRightRadius: 25,
        }}
      >
        <Text
          style={{
            fontSize: 28,
            fontWeight: "bold",
            color: "#8B4513",
            marginBottom: 5,
          }}
        >
          Good{" "}
          {currentTime.getHours() < 12
            ? "Morning"
            : currentTime.getHours() < 18
              ? "Afternoon"
              : "Evening"}
          !
        </Text>
        <Text
          style={{
            fontSize: 18,
            color: "#A0522D",
            fontWeight: "500",
          }}
        >
          {currentTime.toLocaleDateString("en-US", {
            weekday: "long",
            month: "long",
            day: "numeric",
          })}
        </Text>
        <Text
          style={{
            fontSize: 16,
            color: "#A0522D",
            marginTop: 5,
          }}
        >
          {currentTime.toLocaleTimeString("en-US", {
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
          })}
        </Text>
      </View>

      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingBottom: insets.bottom + 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Emergency Button */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <TouchableOpacity
            onPress={handleEmergencyPress}
            style={{
              backgroundColor: "#FF6B6B",
              borderRadius: 20,
              padding: 25,
              alignItems: "center",
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 4 },
              shadowOpacity: 0.1,
              shadowRadius: 8,
              elevation: 5,
            }}
          >
            <Phone size={40} color="white" />
            <Text
              style={{
                fontSize: 24,
                fontWeight: "bold",
                color: "white",
                marginTop: 10,
              }}
            >
              Emergency Help
            </Text>
            <Text
              style={{
                fontSize: 16,
                color: "white",
                opacity: 0.9,
                textAlign: "center",
                marginTop: 5,
              }}
            >
              Tap here or triple-press volume button
            </Text>
          </TouchableOpacity>
        </View>

        {/* Quick Actions */}
        <View style={{ paddingHorizontal: 20, marginTop: 30 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Quick Actions
          </Text>

          <View
            style={{
              flexDirection: "row",
              flexWrap: "wrap",
              justifyContent: "space-between",
            }}
          >
            {quickActions.map((action, index) => (
              <TouchableOpacity
                key={index}
                onPress={action.onPress}
                style={{
                  width: "48%",
                  backgroundColor: action.color,
                  borderRadius: 15,
                  padding: 20,
                  marginBottom: 15,
                  alignItems: "center",
                  shadowColor: "#000",
                  shadowOffset: { width: 0, height: 2 },
                  shadowOpacity: 0.05,
                  shadowRadius: 4,
                  elevation: 2,
                }}
              >
                <action.icon size={32} color={action.iconColor} />
                <Text
                  style={{
                    fontSize: 16,
                    fontWeight: "bold",
                    color: "#2C3E50",
                    marginTop: 10,
                    textAlign: "center",
                  }}
                >
                  {action.title}
                </Text>
                <Text
                  style={{
                    fontSize: 14,
                    color: "#7F8C8D",
                    marginTop: 5,
                    textAlign: "center",
                  }}
                >
                  {action.subtitle}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* Today's Summary */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Today's Summary
          </Text>

          <View
            style={{
              backgroundColor: "white",
              borderRadius: 15,
              padding: 20,
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.05,
              shadowRadius: 4,
              elevation: 2,
            }}
          >
            <View
              style={{
                flexDirection: "row",
                alignItems: "center",
                marginBottom: 15,
              }}
            >
              <Users size={24} color="#3498DB" />
              <Text
                style={{
                  fontSize: 16,
                  color: "#2C3E50",
                  marginLeft: 10,
                  flex: 1,
                }}
              >
                3 volunteers available nearby
              </Text>
            </View>

            <View
              style={{
                flexDirection: "row",
                alignItems: "center",
                marginBottom: 15,
              }}
            >
              <Clock size={24} color="#27AE60" />
              <Text
                style={{
                  fontSize: 16,
                  color: "#2C3E50",
                  marginLeft: 10,
                  flex: 1,
                }}
              >
                Last check-in: 2 hours ago
              </Text>
            </View>

            <View style={{ flexDirection: "row", alignItems: "center" }}>
              <Heart size={24} color="#E74C3C" />
              <Text
                style={{
                  fontSize: 16,
                  color: "#2C3E50",
                  marginLeft: 10,
                  flex: 1,
                }}
              >
                Health status: All good
              </Text>
            </View>
          </View>
        </View>
      </ScrollView>
    </View>
  );
}
