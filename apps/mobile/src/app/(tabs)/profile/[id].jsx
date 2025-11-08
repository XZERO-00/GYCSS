import React from "react";
import { View, Text, ScrollView, TouchableOpacity, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  ArrowLeft,
  Star,
  Phone,
  MessageCircle,
  MapPin,
  Clock,
  Shield,
  Heart,
  Award,
} from "lucide-react-native";
import { router, useLocalSearchParams } from "expo-router";

export default function VolunteerProfile() {
  const insets = useSafeAreaInsets();
  const { id } = useLocalSearchParams();

  // Mock volunteer data - in a real app, this would be fetched based on the ID
  const volunteer = {
    id: 1,
    name: "Sarah Johnson",
    rating: 4.9,
    distance: "0.3 miles",
    specialties: ["Medical Support", "Transportation", "Companionship"],
    availability: "Available now",
    responseTime: "5 min avg",
    completedHelps: 127,
    verified: true,
    avatar: "👩‍⚕️",
    bio: "Retired nurse with 30+ years of experience. I love helping seniors in my community and am available for medical support, transportation, and companionship.",
    languages: ["English", "Spanish"],
    joinedDate: "March 2023",
    badges: ["Top Helper", "Quick Responder", "Medical Expert"],
    reviews: [
      {
        id: 1,
        reviewer: "Margaret K.",
        rating: 5,
        comment:
          "Sarah was incredibly helpful and kind. She helped me get to my doctor appointment and stayed with me during the visit.",
        date: "2 weeks ago",
      },
      {
        id: 2,
        reviewer: "Robert M.",
        rating: 5,
        comment:
          "Very professional and caring. Sarah helped me understand my medication schedule and set up reminders.",
        date: "1 month ago",
      },
      {
        id: 3,
        reviewer: "Dorothy L.",
        rating: 4,
        comment:
          "Great help with grocery shopping. Sarah was patient and made sure I got everything I needed.",
        date: "2 months ago",
      },
    ],
  };

  const handleCallVolunteer = () => {
    Alert.alert(
      "Call Volunteer",
      `Would you like to call ${volunteer.name} for assistance?`,
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Call Now",
          onPress: () =>
            Alert.alert("Calling...", `Connecting you with ${volunteer.name}`),
        },
      ],
    );
  };

  const handleMessage = () => {
    Alert.alert(
      "Message Sent",
      `Your message has been sent to ${volunteer.name}. They will respond shortly.`,
    );
  };

  const handleRequestHelp = () => {
    Alert.alert("Request Help", `Send a help request to ${volunteer.name}?`, [
      { text: "Cancel", style: "cancel" },
      {
        text: "Send Request",
        onPress: () =>
          Alert.alert(
            "Request Sent",
            `Your help request has been sent to ${volunteer.name}. They will respond soon.`,
          ),
      },
    ]);
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
        <View
          style={{
            flexDirection: "row",
            alignItems: "center",
            marginBottom: 20,
          }}
        >
          <TouchableOpacity
            onPress={() => router.back()}
            style={{
              backgroundColor: "rgba(255,255,255,0.2)",
              borderRadius: 12,
              padding: 8,
              marginRight: 15,
            }}
          >
            <ArrowLeft size={24} color="#8B4513" />
          </TouchableOpacity>
          <Text
            style={{
              fontSize: 24,
              fontWeight: "bold",
              color: "#8B4513",
              flex: 1,
            }}
          >
            Volunteer Profile
          </Text>
        </View>

        {/* Volunteer Header */}
        <View style={{ flexDirection: "row", alignItems: "center" }}>
          <View
            style={{
              width: 80,
              height: 80,
              borderRadius: 40,
              backgroundColor: "#F0F8FF",
              alignItems: "center",
              justifyContent: "center",
              marginRight: 20,
            }}
          >
            <Text style={{ fontSize: 32 }}>{volunteer.avatar}</Text>
          </View>

          <View style={{ flex: 1 }}>
            <View
              style={{
                flexDirection: "row",
                alignItems: "center",
                marginBottom: 5,
              }}
            >
              <Text
                style={{
                  fontSize: 24,
                  fontWeight: "bold",
                  color: "#8B4513",
                }}
              >
                {volunteer.name}
              </Text>
              {volunteer.verified && (
                <View
                  style={{
                    backgroundColor: "#27AE60",
                    borderRadius: 12,
                    paddingHorizontal: 8,
                    paddingVertical: 4,
                    marginLeft: 10,
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
                marginBottom: 5,
              }}
            >
              <Star size={18} color="#F39C12" />
              <Text
                style={{
                  fontSize: 16,
                  color: "#A0522D",
                  fontWeight: "600",
                  marginLeft: 5,
                }}
              >
                {volunteer.rating} ({volunteer.completedHelps} helps)
              </Text>
            </View>

            <View style={{ flexDirection: "row", alignItems: "center" }}>
              <MapPin size={16} color="#A0522D" />
              <Text
                style={{
                  fontSize: 14,
                  color: "#A0522D",
                  marginLeft: 5,
                }}
              >
                {volunteer.distance} away
              </Text>
            </View>
          </View>
        </View>
      </View>

      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingBottom: insets.bottom + 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Availability Status */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <View
            style={{
              backgroundColor: volunteer.availability.includes("now")
                ? "#E8F5E8"
                : "#FFF8E1",
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
                justifyContent: "space-between",
              }}
            >
              <View style={{ flexDirection: "row", alignItems: "center" }}>
                <Clock
                  size={20}
                  color={
                    volunteer.availability.includes("now")
                      ? "#27AE60"
                      : "#F39C12"
                  }
                />
                <Text
                  style={{
                    fontSize: 16,
                    fontWeight: "bold",
                    color: volunteer.availability.includes("now")
                      ? "#27AE60"
                      : "#F39C12",
                    marginLeft: 8,
                  }}
                >
                  {volunteer.availability}
                </Text>
              </View>

              <Text
                style={{
                  fontSize: 14,
                  color: "#7F8C8D",
                  fontWeight: "600",
                }}
              >
                Avg response: {volunteer.responseTime}
              </Text>
            </View>
          </View>
        </View>

        {/* Action Buttons */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <View
            style={{
              flexDirection: "row",
              justifyContent: "space-between",
              marginBottom: 15,
            }}
          >
            <TouchableOpacity
              onPress={handleMessage}
              style={{
                backgroundColor: "#E8F5E8",
                borderRadius: 15,
                paddingHorizontal: 20,
                paddingVertical: 15,
                flexDirection: "row",
                alignItems: "center",
                flex: 1,
                marginRight: 8,
              }}
            >
              <MessageCircle size={20} color="#27AE60" />
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "600",
                  color: "#27AE60",
                  marginLeft: 8,
                }}
              >
                Message
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={handleCallVolunteer}
              style={{
                backgroundColor: "#3498DB",
                borderRadius: 15,
                paddingHorizontal: 20,
                paddingVertical: 15,
                flexDirection: "row",
                alignItems: "center",
                flex: 1,
                marginLeft: 8,
              }}
            >
              <Phone size={20} color="white" />
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "600",
                  color: "white",
                  marginLeft: 8,
                }}
              >
                Call
              </Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity
            onPress={handleRequestHelp}
            style={{
              backgroundColor: "#F4D03F",
              borderRadius: 15,
              paddingHorizontal: 20,
              paddingVertical: 15,
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
            <Heart size={20} color="#8B4513" />
            <Text
              style={{
                fontSize: 18,
                fontWeight: "bold",
                color: "#8B4513",
                marginLeft: 8,
              }}
            >
              Request Help
            </Text>
          </TouchableOpacity>
        </View>

        {/* About */}
        <View style={{ paddingHorizontal: 20, marginTop: 30 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            About {volunteer.name}
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
            <Text
              style={{
                fontSize: 16,
                color: "#2C3E50",
                lineHeight: 24,
                marginBottom: 15,
              }}
            >
              {volunteer.bio}
            </Text>

            <View
              style={{
                flexDirection: "row",
                alignItems: "center",
                marginBottom: 10,
              }}
            >
              <Text
                style={{
                  fontSize: 14,
                  color: "#7F8C8D",
                  fontWeight: "600",
                  width: 80,
                }}
              >
                Languages:
              </Text>
              <Text
                style={{
                  fontSize: 14,
                  color: "#2C3E50",
                  flex: 1,
                }}
              >
                {volunteer.languages.join(", ")}
              </Text>
            </View>

            <View style={{ flexDirection: "row", alignItems: "center" }}>
              <Text
                style={{
                  fontSize: 14,
                  color: "#7F8C8D",
                  fontWeight: "600",
                  width: 80,
                }}
              >
                Joined:
              </Text>
              <Text
                style={{
                  fontSize: 14,
                  color: "#2C3E50",
                  flex: 1,
                }}
              >
                {volunteer.joinedDate}
              </Text>
            </View>
          </View>
        </View>

        {/* Specialties */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Specialties
          </Text>

          <View style={{ flexDirection: "row", flexWrap: "wrap" }}>
            {volunteer.specialties.map((specialty, index) => (
              <View
                key={index}
                style={{
                  backgroundColor: "#F0F8FF",
                  borderRadius: 20,
                  paddingHorizontal: 15,
                  paddingVertical: 10,
                  marginRight: 10,
                  marginBottom: 10,
                  shadowColor: "#000",
                  shadowOffset: { width: 0, height: 1 },
                  shadowOpacity: 0.05,
                  shadowRadius: 2,
                  elevation: 1,
                }}
              >
                <Text
                  style={{
                    fontSize: 14,
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

        {/* Badges */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Achievements
          </Text>

          <View style={{ flexDirection: "row", flexWrap: "wrap" }}>
            {volunteer.badges.map((badge, index) => (
              <View
                key={index}
                style={{
                  backgroundColor: "#FFF8E1",
                  borderRadius: 15,
                  paddingHorizontal: 12,
                  paddingVertical: 8,
                  marginRight: 10,
                  marginBottom: 10,
                  flexDirection: "row",
                  alignItems: "center",
                  shadowColor: "#000",
                  shadowOffset: { width: 0, height: 1 },
                  shadowOpacity: 0.05,
                  shadowRadius: 2,
                  elevation: 1,
                }}
              >
                <Award size={16} color="#F39C12" />
                <Text
                  style={{
                    fontSize: 12,
                    color: "#F39C12",
                    fontWeight: "600",
                    marginLeft: 5,
                  }}
                >
                  {badge}
                </Text>
              </View>
            ))}
          </View>
        </View>

        {/* Reviews */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Recent Reviews
          </Text>

          {volunteer.reviews.map((review) => (
            <View
              key={review.id}
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
              }}
            >
              <View
                style={{
                  flexDirection: "row",
                  alignItems: "center",
                  marginBottom: 10,
                }}
              >
                <Text
                  style={{
                    fontSize: 16,
                    fontWeight: "bold",
                    color: "#2C3E50",
                    flex: 1,
                  }}
                >
                  {review.reviewer}
                </Text>

                <View style={{ flexDirection: "row", alignItems: "center" }}>
                  {[...Array(review.rating)].map((_, i) => (
                    <Star key={i} size={14} color="#F39C12" />
                  ))}
                </View>
              </View>

              <Text
                style={{
                  fontSize: 14,
                  color: "#2C3E50",
                  lineHeight: 20,
                  marginBottom: 10,
                }}
              >
                "{review.comment}"
              </Text>

              <Text
                style={{
                  fontSize: 12,
                  color: "#7F8C8D",
                }}
              >
                {review.date}
              </Text>
            </View>
          ))}
        </View>
      </ScrollView>
    </View>
  );
}
