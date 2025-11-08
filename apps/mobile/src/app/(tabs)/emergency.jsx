import React, { useState, useEffect } from "react";
import { View, Text, TouchableOpacity, Alert, Animated } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  Phone,
  MapPin,
  Clock,
  X,
  CheckCircle,
  AlertTriangle,
} from "lucide-react-native";
import { router } from "expo-router";

export default function Emergency() {
  const insets = useSafeAreaInsets();
  const [emergencyActive, setEmergencyActive] = useState(true);
  const [timeElapsed, setTimeElapsed] = useState(0);
  const [pulseAnim] = useState(new Animated.Value(1));
  const [volunteersNotified, setVolunteersNotified] = useState([
    {
      name: "Sarah Johnson",
      distance: "0.3 miles",
      eta: "5 min",
      status: "responding",
    },
    {
      name: "Michael Chen",
      distance: "0.5 miles",
      eta: "8 min",
      status: "notified",
    },
    {
      name: "Emma Rodriguez",
      distance: "0.7 miles",
      eta: "12 min",
      status: "notified",
    },
  ]);

  useEffect(() => {
    // Pulse animation for emergency button
    const pulse = () => {
      Animated.sequence([
        Animated.timing(pulseAnim, {
          toValue: 1.1,
          duration: 800,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnim, {
          toValue: 1,
          duration: 800,
          useNativeDriver: true,
        }),
      ]).start(() => pulse());
    };
    pulse();

    // Timer
    const timer = setInterval(() => {
      setTimeElapsed((prev) => prev + 1);
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  const handleCancelEmergency = () => {
    Alert.alert(
      "Cancel Emergency",
      "Are you sure you want to cancel the emergency alert? This will notify volunteers that you no longer need assistance.",
      [
        { text: "Keep Active", style: "cancel" },
        {
          text: "Cancel Emergency",
          style: "destructive",
          onPress: () => {
            setEmergencyActive(false);
            Alert.alert(
              "Emergency Cancelled",
              "All volunteers have been notified that you no longer need assistance.",
            );
            setTimeout(() => router.back(), 2000);
          },
        },
      ],
    );
  };

  const handleCallVolunteer = (volunteer) => {
    Alert.alert("Call Volunteer", `Call ${volunteer.name} directly?`, [
      { text: "Cancel", style: "cancel" },
      {
        text: "Call Now",
        onPress: () =>
          Alert.alert("Calling...", `Connecting you with ${volunteer.name}`),
      },
    ]);
  };

  if (!emergencyActive) {
    return (
      <View
        style={{
          flex: 1,
          backgroundColor: "#FFF8F0",
          alignItems: "center",
          justifyContent: "center",
          paddingHorizontal: 20,
        }}
      >
        <StatusBar style="dark" />
        <CheckCircle size={80} color="#27AE60" />
        <Text
          style={{
            fontSize: 24,
            fontWeight: "bold",
            color: "#27AE60",
            textAlign: "center",
            marginTop: 20,
          }}
        >
          Emergency Cancelled
        </Text>
        <Text
          style={{
            fontSize: 16,
            color: "#7F8C8D",
            textAlign: "center",
            marginTop: 10,
          }}
        >
          All volunteers have been notified
        </Text>
      </View>
    );
  }

  return (
    <View style={{ flex: 1, backgroundColor: "#FF6B6B" }}>
      <StatusBar style="light" />

      {/* Header */}
      <View
        style={{
          paddingTop: insets.top + 20,
          paddingHorizontal: 20,
          paddingBottom: 30,
          alignItems: "center",
        }}
      >
        <AlertTriangle size={40} color="white" />
        <Text
          style={{
            fontSize: 32,
            fontWeight: "bold",
            color: "white",
            marginTop: 10,
            textAlign: "center",
          }}
        >
          EMERGENCY ACTIVE
        </Text>
        <Text
          style={{
            fontSize: 18,
            color: "rgba(255,255,255,0.9)",
            textAlign: "center",
            marginTop: 5,
          }}
        >
          Help is on the way
        </Text>

        {/* Timer */}
        <View
          style={{
            backgroundColor: "rgba(255,255,255,0.2)",
            borderRadius: 15,
            paddingHorizontal: 20,
            paddingVertical: 10,
            marginTop: 15,
            flexDirection: "row",
            alignItems: "center",
          }}
        >
          <Clock size={20} color="white" />
          <Text
            style={{
              fontSize: 18,
              fontWeight: "bold",
              color: "white",
              marginLeft: 8,
            }}
          >
            {formatTime(timeElapsed)}
          </Text>
        </View>
      </View>

      {/* Main Content */}
      <View
        style={{
          flex: 1,
          backgroundColor: "#FFF8F0",
          borderTopLeftRadius: 30,
          borderTopRightRadius: 30,
          paddingTop: 30,
          paddingHorizontal: 20,
        }}
      >
        {/* Location Status */}
        <View
          style={{
            backgroundColor: "white",
            borderRadius: 15,
            padding: 20,
            marginBottom: 25,
            shadowColor: "#000",
            shadowOffset: { width: 0, height: 4 },
            shadowOpacity: 0.1,
            shadowRadius: 8,
            elevation: 5,
          }}
        >
          <View
            style={{
              flexDirection: "row",
              alignItems: "center",
              marginBottom: 10,
            }}
          >
            <MapPin size={24} color="#3498DB" />
            <Text
              style={{
                fontSize: 18,
                fontWeight: "bold",
                color: "#2C3E50",
                marginLeft: 10,
              }}
            >
              Your Location Shared
            </Text>
          </View>
          <Text
            style={{
              fontSize: 14,
              color: "#7F8C8D",
              lineHeight: 20,
            }}
          >
            Your exact location has been shared with nearby volunteers and
            emergency contacts. They can see your precise location to reach you
            quickly.
          </Text>
        </View>

        {/* Volunteers Responding */}
        <Text
          style={{
            fontSize: 22,
            fontWeight: "bold",
            color: "#8B4513",
            marginBottom: 15,
          }}
        >
          Volunteers Responding
        </Text>

        {volunteersNotified.map((volunteer, index) => (
          <View
            key={index}
            style={{
              backgroundColor: "white",
              borderRadius: 15,
              padding: 20,
              marginBottom: 15,
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.05,
              shadowRadius: 4,
              elevation: 2,
              borderLeftWidth: 4,
              borderLeftColor:
                volunteer.status === "responding" ? "#27AE60" : "#F39C12",
            }}
          >
            <View
              style={{
                flexDirection: "row",
                alignItems: "center",
                justifyContent: "space-between",
              }}
            >
              <View style={{ flex: 1 }}>
                <Text
                  style={{
                    fontSize: 16,
                    fontWeight: "bold",
                    color: "#2C3E50",
                  }}
                >
                  {volunteer.name}
                </Text>
                <View
                  style={{
                    flexDirection: "row",
                    alignItems: "center",
                    marginTop: 5,
                  }}
                >
                  <MapPin size={16} color="#7F8C8D" />
                  <Text
                    style={{
                      fontSize: 14,
                      color: "#7F8C8D",
                      marginLeft: 4,
                    }}
                  >
                    {volunteer.distance} away • ETA {volunteer.eta}
                  </Text>
                </View>
                <View
                  style={{
                    backgroundColor:
                      volunteer.status === "responding" ? "#E8F5E8" : "#FFF8E1",
                    borderRadius: 8,
                    paddingHorizontal: 8,
                    paddingVertical: 4,
                    alignSelf: "flex-start",
                    marginTop: 8,
                  }}
                >
                  <Text
                    style={{
                      fontSize: 12,
                      fontWeight: "600",
                      color:
                        volunteer.status === "responding"
                          ? "#27AE60"
                          : "#F39C12",
                    }}
                  >
                    {volunteer.status === "responding"
                      ? "ON THE WAY"
                      : "NOTIFIED"}
                  </Text>
                </View>
              </View>

              <TouchableOpacity
                onPress={() => handleCallVolunteer(volunteer)}
                style={{
                  backgroundColor: "#3498DB",
                  borderRadius: 12,
                  padding: 12,
                  marginLeft: 15,
                }}
              >
                <Phone size={20} color="white" />
              </TouchableOpacity>
            </View>
          </View>
        ))}

        {/* Cancel Emergency Button */}
        <View style={{ marginTop: 30, alignItems: "center" }}>
          <TouchableOpacity
            onPress={handleCancelEmergency}
            style={{
              backgroundColor: "white",
              borderRadius: 15,
              paddingHorizontal: 30,
              paddingVertical: 15,
              flexDirection: "row",
              alignItems: "center",
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.1,
              shadowRadius: 4,
              elevation: 3,
              borderWidth: 2,
              borderColor: "#E74C3C",
            }}
          >
            <X size={20} color="#E74C3C" />
            <Text
              style={{
                fontSize: 16,
                fontWeight: "600",
                color: "#E74C3C",
                marginLeft: 8,
              }}
            >
              Cancel Emergency
            </Text>
          </TouchableOpacity>

          <Text
            style={{
              fontSize: 12,
              color: "#7F8C8D",
              textAlign: "center",
              marginTop: 10,
              paddingHorizontal: 20,
            }}
          >
            Only cancel if you no longer need assistance. This will notify all
            volunteers.
          </Text>
        </View>

        {/* Emergency Instructions */}
        <View
          style={{
            backgroundColor: "#FFF8E1",
            borderRadius: 15,
            padding: 20,
            marginTop: 20,
            marginBottom: insets.bottom + 20,
          }}
        >
          <Text
            style={{
              fontSize: 16,
              fontWeight: "bold",
              color: "#F39C12",
              marginBottom: 10,
            }}
          >
            While You Wait:
          </Text>
          <Text
            style={{
              fontSize: 14,
              color: "#8B4513",
              lineHeight: 20,
            }}
          >
            • Stay calm and in a safe location{"\n"}• Keep your phone nearby
            {"\n"}• If this is a medical emergency, call 911{"\n"}• Volunteers
            will contact you when they arrive
          </Text>
        </View>
      </View>
    </View>
  );
}
