import { Tabs } from "expo-router";
import { Heart, User, FileText, Bell } from "lucide-react-native";

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: "#FFF8F0", // Warm ivory background
          borderTopWidth: 1,
          borderColor: "#F4D03F", // Soft gold border
          paddingTop: 4,
          height: 85,
        },
        tabBarActiveTintColor: "#D68910", // Warm gold
        tabBarInactiveTintColor: "#85929E", // Soft gray
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: "600",
        },
      }}
    >
      <Tabs.Screen
        name="dashboard"
        options={{
          title: "Home",
          tabBarIcon: ({ color, size }) => <Heart color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="health"
        options={{
          title: "Health",
          tabBarIcon: ({ color, size }) => <FileText color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="volunteers"
        options={{
          title: "Volunteers",
          tabBarIcon: ({ color, size }) => <User color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="notifications"
        options={{
          title: "Alerts",
          tabBarIcon: ({ color, size }) => <Bell color={color} size={24} />,
        }}
      />
      <Tabs.Screen
        name="profile/[id]"
        options={{
          href: null, // Hidden from tab bar
        }}
      />
      <Tabs.Screen
        name="emergency"
        options={{
          href: null, // Hidden from tab bar
        }}
      />
    </Tabs>
  );
}
