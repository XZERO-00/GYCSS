import React, { useState } from "react";
import { View, Text, ScrollView, TouchableOpacity, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  Bell,
  Heart,
  Phone,
  MapPin,
  Clock,
  CheckCircle,
  AlertTriangle,
  Settings,
} from "lucide-react-native";

export default function Notifications() {
  const insets = useSafeAreaInsets();
  const [notifications, setNotifications] = useState([
    {
      id: 1,
      type: "volunteer_response",
      title: "Sarah Johnson responded",
      message:
        "Sarah is on her way to help you with grocery shopping. ETA: 15 minutes.",
      time: "5 min ago",
      read: false,
      icon: Heart,
      color: "#27AE60",
    },
    {
      id: 2,
      type: "emergency_alert",
      title: "Emergency contact notified",
      message:
        "Your emergency contact has been notified of your safety check request.",
      time: "1 hour ago",
      read: false,
      icon: AlertTriangle,
      color: "#E74C3C",
    },
    {
      id: 3,
      type: "health_reminder",
      title: "Medication reminder",
      message:
        "Time to take your Lisinopril 10mg. Don't forget to take it with food.",
      time: "2 hours ago",
      read: true,
      icon: Clock,
      color: "#F39C12",
    },
    {
      id: 4,
      type: "volunteer_nearby",
      title: "New volunteer nearby",
      message:
        "Michael Chen just became available in your area. He specializes in technology help.",
      time: "3 hours ago",
      read: true,
      icon: MapPin,
      color: "#3498DB",
    },
    {
      id: 5,
      type: "check_in",
      title: "Daily wellness check",
      message:
        "How are you feeling today? Tap to complete your daily wellness check.",
      time: "6 hours ago",
      read: true,
      icon: Heart,
      color: "#9B59B6",
    },
    {
      id: 6,
      type: "appointment",
      title: "Upcoming appointment",
      message:
        "Reminder: You have a doctor's appointment tomorrow at 2:00 PM with Dr. Smith.",
      time: "1 day ago",
      read: true,
      icon: Clock,
      color: "#F39C12",
    },
  ]);

  const handleNotificationPress = (notification) => {
    // Mark as read
    setNotifications((prev) =>
      prev.map((n) => (n.id === notification.id ? { ...n, read: true } : n)),
    );

    // Handle different notification types
    switch (notification.type) {
      case "volunteer_response":
        Alert.alert("Volunteer Update", notification.message);
        break;
      case "emergency_alert":
        Alert.alert("Emergency Status", notification.message);
        break;
      case "health_reminder":
        Alert.alert("Health Reminder", notification.message, [
          { text: "Remind Later", style: "cancel" },
          {
            text: "Mark as Taken",
            onPress: () => Alert.alert("Marked", "Medication marked as taken"),
          },
        ]);
        break;
      case "volunteer_nearby":
        Alert.alert("New Volunteer", notification.message);
        break;
      case "check_in":
        Alert.alert("Wellness Check", "How are you feeling today?", [
          { text: "Not Great", style: "destructive" },
          { text: "Okay", style: "default" },
          { text: "Great!", style: "default" },
        ]);
        break;
      case "appointment":
        Alert.alert("Appointment Reminder", notification.message);
        break;
      default:
        Alert.alert("Notification", notification.message);
    }
  };

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    Alert.alert("Success", "All notifications marked as read");
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

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
            justifyContent: "space-between",
          }}
        >
          <View>
            <Text
              style={{
                fontSize: 28,
                fontWeight: "bold",
                color: "#8B4513",
                marginBottom: 5,
              }}
            >
              Notifications
            </Text>
            <Text
              style={{
                fontSize: 16,
                color: "#A0522D",
              }}
            >
              {unreadCount > 0 ? `${unreadCount} new alerts` : "All caught up!"}
            </Text>
          </View>

          <TouchableOpacity
            onPress={() =>
              Alert.alert("Settings", "Notification settings coming soon!")
            }
            style={{
              backgroundColor: "rgba(255,255,255,0.2)",
              borderRadius: 12,
              padding: 10,
            }}
          >
            <Settings size={24} color="#8B4513" />
          </TouchableOpacity>
        </View>
      </View>

      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingBottom: insets.bottom + 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Quick Actions */}
        {unreadCount > 0 && (
          <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
            <TouchableOpacity
              onPress={markAllAsRead}
              style={{
                backgroundColor: "#E8F5E8",
                borderRadius: 15,
                padding: 15,
                flexDirection: "row",
                alignItems: "center",
                justifyContent: "center",
                shadowColor: "#000",
                shadowOffset: { width: 0, height: 2 },
                shadowOpacity: 0.05,
                shadowRadius: 4,
                elevation: 2,
              }}
            >
              <CheckCircle size={20} color="#27AE60" />
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "600",
                  color: "#27AE60",
                  marginLeft: 8,
                }}
              >
                Mark All as Read
              </Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Notifications List */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Recent Alerts
          </Text>

          {notifications.map((notification) => (
            <TouchableOpacity
              key={notification.id}
              onPress={() => handleNotificationPress(notification)}
              style={{
                backgroundColor: notification.read ? "white" : "#F8F9FA",
                borderRadius: 15,
                padding: 20,
                marginBottom: 12,
                shadowColor: "#000",
                shadowOffset: { width: 0, height: 2 },
                shadowOpacity: notification.read ? 0.03 : 0.08,
                shadowRadius: 4,
                elevation: notification.read ? 1 : 3,
                borderLeftWidth: 4,
                borderLeftColor: notification.color,
              }}
            >
              <View style={{ flexDirection: "row", alignItems: "flex-start" }}>
                {/* Icon */}
                <View
                  style={{
                    width: 40,
                    height: 40,
                    borderRadius: 20,
                    backgroundColor: `${notification.color}15`,
                    alignItems: "center",
                    justifyContent: "center",
                    marginRight: 15,
                  }}
                >
                  <notification.icon size={20} color={notification.color} />
                </View>

                {/* Content */}
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
                        fontSize: 16,
                        fontWeight: "bold",
                        color: "#2C3E50",
                        flex: 1,
                      }}
                    >
                      {notification.title}
                    </Text>

                    {!notification.read && (
                      <View
                        style={{
                          width: 8,
                          height: 8,
                          borderRadius: 4,
                          backgroundColor: notification.color,
                          marginLeft: 8,
                        }}
                      />
                    )}
                  </View>

                  <Text
                    style={{
                      fontSize: 14,
                      color: "#7F8C8D",
                      lineHeight: 20,
                      marginBottom: 8,
                    }}
                  >
                    {notification.message}
                  </Text>

                  <Text
                    style={{
                      fontSize: 12,
                      color: "#95A5A6",
                      fontWeight: "500",
                    }}
                  >
                    {notification.time}
                  </Text>
                </View>
              </View>
            </TouchableOpacity>
          ))}
        </View>

        {/* Emergency Notification Settings */}
        <View style={{ paddingHorizontal: 20, marginTop: 30 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Emergency Settings
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
              <Phone size={20} color="#E74C3C" />
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "600",
                  color: "#2C3E50",
                  marginLeft: 10,
                  flex: 1,
                }}
              >
                Emergency Contacts
              </Text>
              <TouchableOpacity
                onPress={() =>
                  Alert.alert(
                    "Emergency Contacts",
                    "Manage your emergency contacts",
                  )
                }
                style={{
                  backgroundColor: "#F0F8FF",
                  paddingHorizontal: 12,
                  paddingVertical: 6,
                  borderRadius: 8,
                }}
              >
                <Text
                  style={{
                    fontSize: 12,
                    color: "#3498DB",
                    fontWeight: "600",
                  }}
                >
                  MANAGE
                </Text>
              </TouchableOpacity>
            </View>

            <Text
              style={{
                fontSize: 14,
                color: "#7F8C8D",
                marginBottom: 15,
              }}
            >
              2 emergency contacts configured
            </Text>

            <View style={{ flexDirection: "row", alignItems: "center" }}>
              <AlertTriangle size={20} color="#F39C12" />
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "600",
                  color: "#2C3E50",
                  marginLeft: 10,
                  flex: 1,
                }}
              >
                Volume Button Emergency
              </Text>
              <View
                style={{
                  backgroundColor: "#E8F5E8",
                  paddingHorizontal: 12,
                  paddingVertical: 6,
                  borderRadius: 8,
                }}
              >
                <Text
                  style={{
                    fontSize: 12,
                    color: "#27AE60",
                    fontWeight: "600",
                  }}
                >
                  ENABLED
                </Text>
              </View>
            </View>

            <Text
              style={{
                fontSize: 14,
                color: "#7F8C8D",
                marginTop: 8,
              }}
            >
              Triple-press volume button to trigger emergency alert
            </Text>
          </View>
        </View>

        {/* Notification Preferences */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <TouchableOpacity
            onPress={() =>
              Alert.alert(
                "Notification Preferences",
                "Customize your notification settings",
              )
            }
            style={{
              backgroundColor: "#F4D03F",
              borderRadius: 15,
              padding: 20,
              flexDirection: "row",
              alignItems: "center",
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.05,
              shadowRadius: 4,
              elevation: 2,
            }}
          >
            <Bell size={24} color="#8B4513" />
            <View style={{ flex: 1, marginLeft: 15 }}>
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "bold",
                  color: "#8B4513",
                }}
              >
                Notification Preferences
              </Text>
              <Text
                style={{
                  fontSize: 14,
                  color: "#A0522D",
                  marginTop: 2,
                }}
              >
                Customize when and how you receive alerts
              </Text>
            </View>
            <Settings size={20} color="#8B4513" />
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}
