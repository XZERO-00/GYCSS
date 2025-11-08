import React, { useState } from "react";
import { View, Text, ScrollView, TouchableOpacity, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  User,
  Phone,
  MapPin,
  Star,
  Clock,
  Heart,
  MessageCircle,
} from "lucide-react-native";
import { router } from "expo-router";

export default function Volunteers() {
  const insets = useSafeAreaInsets();
  const [selectedFilter, setSelectedFilter] = useState("nearby");

  const volunteers = [
    {
      id: 1,
      name: "Sarah Johnson",
      rating: 4.9,
      distance: "0.3 miles",
      specialties: ["Medical Support", "Transportation"],
      availability: "Available now",
      responseTime: "5 min avg",
      completedHelps: 127,
      verified: true,
      avatar: "👩‍⚕️",
    },
    {
      id: 2,
      name: "Michael Chen",
      rating: 4.8,
      distance: "0.5 miles",
      specialties: ["Grocery Shopping", "Technology Help"],
      availability: "Available now",
      responseTime: "8 min avg",
      completedHelps: 89,
      verified: true,
      avatar: "👨‍💼",
    },
    {
      id: 3,
      name: "Emma Rodriguez",
      rating: 4.9,
      distance: "0.7 miles",
      specialties: ["Companionship", "Pet Care"],
      availability: "Available in 30 min",
      responseTime: "3 min avg",
      completedHelps: 156,
      verified: true,
      avatar: "👩‍🦳",
    },
    {
      id: 4,
      name: "David Thompson",
      rating: 4.7,
      distance: "1.2 miles",
      specialties: ["Home Repairs", "Transportation"],
      availability: "Available now",
      responseTime: "12 min avg",
      completedHelps: 73,
      verified: true,
      avatar: "👨‍🔧",
    },
  ];

  const filters = [
    { id: "nearby", label: "Nearby", count: 4 },
    { id: "available", label: "Available Now", count: 3 },
    { id: "medical", label: "Medical Support", count: 2 },
    { id: "transport", label: "Transportation", count: 2 },
  ];

  const handleCallVolunteer = (volunteer) => {
    Alert.alert(
      "Call Volunteer",
      `Would you like to call ${volunteer.name} for assistance?`,
      [
        { text: "Cancel", style: "cancel" },
        { text: "Message First", onPress: () => handleMessage(volunteer) },
        {
          text: "Call Now",
          onPress: () =>
            Alert.alert("Calling...", `Connecting you with ${volunteer.name}`),
        },
      ],
    );
  };

  const handleMessage = (volunteer) => {
    Alert.alert(
      "Message Sent",
      `Your message has been sent to ${volunteer.name}. They will respond shortly.`,
    );
  };

  const handleViewProfile = (volunteer) => {
    router.push(`/(tabs)/profile/${volunteer.id}`);
  };

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
          Volunteers
        </Text>
        <Text
          style={{
            fontSize: 16,
            color: "#A0522D",
          }}
        >
          Caring people ready to help you
        </Text>
      </View>

      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingBottom: insets.bottom + 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Emergency Call Button */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <TouchableOpacity
            onPress={() =>
              Alert.alert("Emergency", "Calling nearest available volunteer...")
            }
            style={{
              backgroundColor: "#FF6B6B",
              borderRadius: 15,
              padding: 20,
              flexDirection: "row",
              alignItems: "center",
              justifyContent: "center",
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 4 },
              shadowOpacity: 0.1,
              shadowRadius: 8,
              elevation: 5,
            }}
          >
            <Phone size={24} color="white" />
            <Text
              style={{
                fontSize: 18,
                fontWeight: "bold",
                color: "white",
                marginLeft: 10,
              }}
            >
              Emergency Call - Get Help Now
            </Text>
          </TouchableOpacity>
        </View>

        {/* Filters */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Find Volunteers
          </Text>

          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={{ marginBottom: 20 }}
          >
            {filters.map((filter) => (
              <TouchableOpacity
                key={filter.id}
                onPress={() => setSelectedFilter(filter.id)}
                style={{
                  backgroundColor:
                    selectedFilter === filter.id ? "#D68910" : "white",
                  borderRadius: 20,
                  paddingHorizontal: 16,
                  paddingVertical: 10,
                  marginRight: 10,
                  shadowColor: "#000",
                  shadowOffset: { width: 0, height: 2 },
                  shadowOpacity: 0.05,
                  shadowRadius: 4,
                  elevation: 2,
                }}
              >
                <Text
                  style={{
                    fontSize: 14,
                    fontWeight: "600",
                    color: selectedFilter === filter.id ? "white" : "#2C3E50",
                  }}
                >
                  {filter.label} ({filter.count})
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* Volunteers List */}
        <View style={{ paddingHorizontal: 20 }}>
          {volunteers.map((volunteer) => (
            <TouchableOpacity
              key={volunteer.id}
              onPress={() => handleViewProfile(volunteer)}
              style={{
                backgroundColor: "white",
                borderRadius: 20,
                padding: 20,
                marginBottom: 15,
                shadowColor: "#000",
                shadowOffset: { width: 0, height: 4 },
                shadowOpacity: 0.08,
                shadowRadius: 8,
                elevation: 4,
              }}
            >
              {/* Volunteer Header */}
              <View
                style={{
                  flexDirection: "row",
                  alignItems: "center",
                  marginBottom: 15,
                }}
              >
                <View
                  style={{
                    width: 60,
                    height: 60,
                    borderRadius: 30,
                    backgroundColor: "#F0F8FF",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  <Text style={{ fontSize: 24 }}>{volunteer.avatar}</Text>
                </View>

                <View style={{ flex: 1, marginLeft: 15 }}>
                  <View style={{ flexDirection: "row", alignItems: "center" }}>
                    <Text
                      style={{
                        fontSize: 18,
                        fontWeight: "bold",
                        color: "#2C3E50",
                      }}
                    >
                      {volunteer.name}
                    </Text>
                    {volunteer.verified && (
                      <View
                        style={{
                          backgroundColor: "#27AE60",
                          borderRadius: 10,
                          paddingHorizontal: 6,
                          paddingVertical: 2,
                          marginLeft: 8,
                        }}
                      >
                        <Text
                          style={{
                            fontSize: 10,
                            color: "white",
                            fontWeight: "600",
                          }}
                        >
                          VERIFIED
                        </Text>
                      </View>
                    )}
                  </View>

                  <View
                    style={{
                      flexDirection: "row",
                      alignItems: "center",
                      marginTop: 5,
                    }}
                  >
                    <Star size={16} color="#F39C12" />
                    <Text
                      style={{
                        fontSize: 14,
                        color: "#F39C12",
                        fontWeight: "600",
                        marginLeft: 4,
                      }}
                    >
                      {volunteer.rating}
                    </Text>
                    <Text
                      style={{
                        fontSize: 14,
                        color: "#7F8C8D",
                        marginLeft: 8,
                      }}
                    >
                      ({volunteer.completedHelps} helps)
                    </Text>
                  </View>
                </View>
              </View>

              {/* Availability Status */}
              <View
                style={{
                  backgroundColor: volunteer.availability.includes("now")
                    ? "#E8F5E8"
                    : "#FFF8E1",
                  borderRadius: 10,
                  padding: 12,
                  marginBottom: 15,
                }}
              >
                <View
                  style={{
                    flexDirection: "row",
                    alignItems: "center",
                    justifyContent: "space-between",
                  }}
                >
                  <View style={{ flexDirection: "row", alignItems: "center" }}>
                    <Clock
                      size={16}
                      color={
                        volunteer.availability.includes("now")
                          ? "#27AE60"
                          : "#F39C12"
                      }
                    />
                    <Text
                      style={{
                        fontSize: 14,
                        fontWeight: "600",
                        color: volunteer.availability.includes("now")
                          ? "#27AE60"
                          : "#F39C12",
                        marginLeft: 6,
                      }}
                    >
                      {volunteer.availability}
                    </Text>
                  </View>

                  <View style={{ flexDirection: "row", alignItems: "center" }}>
                    <MapPin size={16} color="#3498DB" />
                    <Text
                      style={{
                        fontSize: 14,
                        color: "#3498DB",
                        fontWeight: "600",
                        marginLeft: 4,
                      }}
                    >
                      {volunteer.distance}
                    </Text>
                  </View>
                </View>
              </View>

              {/* Specialties */}
              <View style={{ marginBottom: 15 }}>
                <Text
                  style={{
                    fontSize: 14,
                    color: "#7F8C8D",
                    marginBottom: 8,
                  }}
                >
                  Specialties:
                </Text>
                <View style={{ flexDirection: "row", flexWrap: "wrap" }}>
                  {volunteer.specialties.map((specialty, index) => (
                    <View
                      key={index}
                      style={{
                        backgroundColor: "#F0F8FF",
                        borderRadius: 15,
                        paddingHorizontal: 10,
                        paddingVertical: 5,
                        marginRight: 8,
                        marginBottom: 5,
                      }}
                    >
                      <Text
                        style={{
                          fontSize: 12,
                          color: "#3498DB",
                          fontWeight: "600",
                        }}
                      >
                        {specialty}
                      </Text>
                    </View>
                  ))}
                </View>
              </View>

              {/* Action Buttons */}
              <View
                style={{
                  flexDirection: "row",
                  justifyContent: "space-between",
                }}
              >
                <TouchableOpacity
                  onPress={() => handleMessage(volunteer)}
                  style={{
                    backgroundColor: "#E8F5E8",
                    borderRadius: 12,
                    paddingHorizontal: 20,
                    paddingVertical: 12,
                    flexDirection: "row",
                    alignItems: "center",
                    flex: 1,
                    marginRight: 8,
                  }}
                >
                  <MessageCircle size={18} color="#27AE60" />
                  <Text
                    style={{
                      fontSize: 14,
                      fontWeight: "600",
                      color: "#27AE60",
                      marginLeft: 6,
                    }}
                  >
                    Message
                  </Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => handleCallVolunteer(volunteer)}
                  style={{
                    backgroundColor: "#3498DB",
                    borderRadius: 12,
                    paddingHorizontal: 20,
                    paddingVertical: 12,
                    flexDirection: "row",
                    alignItems: "center",
                    flex: 1,
                    marginLeft: 8,
                  }}
                >
                  <Phone size={18} color="white" />
                  <Text
                    style={{
                      fontSize: 14,
                      fontWeight: "600",
                      color: "white",
                      marginLeft: 6,
                    }}
                  >
                    Call Now
                  </Text>
                </TouchableOpacity>
              </View>

              {/* Response Time */}
              <View
                style={{
                  alignItems: "center",
                  marginTop: 12,
                  paddingTop: 12,
                  borderTopWidth: 1,
                  borderTopColor: "#F8F9FA",
                }}
              >
                <Text
                  style={{
                    fontSize: 12,
                    color: "#7F8C8D",
                  }}
                >
                  Average response time: {volunteer.responseTime}
                </Text>
              </View>
            </TouchableOpacity>
          ))}
        </View>

        {/* Become a Volunteer */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <TouchableOpacity
            style={{
              backgroundColor: "#F4D03F",
              borderRadius: 15,
              padding: 20,
              alignItems: "center",
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.05,
              shadowRadius: 4,
              elevation: 2,
            }}
          >
            <Heart size={24} color="#8B4513" />
            <Text
              style={{
                fontSize: 16,
                fontWeight: "bold",
                color: "#8B4513",
                marginTop: 8,
              }}
            >
              Want to Help Others?
            </Text>
            <Text
              style={{
                fontSize: 14,
                color: "#A0522D",
                textAlign: "center",
                marginTop: 4,
              }}
            >
              Join our volunteer community and make a difference
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}
