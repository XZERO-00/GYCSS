import React, { useState } from "react";
import { View, Text, ScrollView, TouchableOpacity, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  Heart,
  FileText,
  Upload,
  Calendar,
  Pill,
  Activity,
  Plus,
} from "lucide-react-native";
import useUpload from "@/utils/useUpload";

export default function Health() {
  const insets = useSafeAreaInsets();
  const [upload, { loading }] = useUpload();
  const [uploadedFiles, setUploadedFiles] = useState([]);

  const handleFileUpload = async () => {
    try {
      // In a real app, you'd use expo-document-picker here
      Alert.alert("Upload Document", "Choose document type:", [
        { text: "Cancel", style: "cancel" },
        {
          text: "Medical Report",
          onPress: () => simulateUpload("Medical Report"),
        },
        { text: "Prescription", onPress: () => simulateUpload("Prescription") },
        { text: "Lab Results", onPress: () => simulateUpload("Lab Results") },
      ]);
    } catch (error) {
      Alert.alert("Error", "Failed to upload document");
    }
  };

  const simulateUpload = (type) => {
    const newFile = {
      id: Date.now(),
      name: `${type}_${new Date().toLocaleDateString()}`,
      type: type,
      date: new Date().toLocaleDateString(),
      size: "2.3 MB",
    };
    setUploadedFiles((prev) => [newFile, ...prev]);
    Alert.alert("Success", `${type} uploaded successfully!`);
  };

  const healthMetrics = [
    {
      title: "Blood Pressure",
      value: "120/80",
      status: "Normal",
      color: "#27AE60",
      icon: Activity,
    },
    {
      title: "Heart Rate",
      value: "72 bpm",
      status: "Good",
      color: "#E74C3C",
      icon: Heart,
    },
    {
      title: "Weight",
      value: "68 kg",
      status: "Stable",
      color: "#3498DB",
      icon: Activity,
    },
    {
      title: "Temperature",
      value: "98.6°F",
      status: "Normal",
      color: "#F39C12",
      icon: Activity,
    },
  ];

  const recentRecords = [
    {
      id: 1,
      title: "Annual Checkup",
      doctor: "Dr. Smith",
      date: "2024-10-15",
      type: "Checkup",
    },
    {
      id: 2,
      title: "Blood Test Results",
      doctor: "Dr. Johnson",
      date: "2024-10-10",
      type: "Lab Results",
    },
    {
      id: 3,
      title: "Prescription Refill",
      doctor: "Dr. Brown",
      date: "2024-10-05",
      type: "Prescription",
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
          Health Records
        </Text>
        <Text
          style={{
            fontSize: 16,
            color: "#A0522D",
          }}
        >
          Your medical information at a glance
        </Text>
      </View>

      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingBottom: insets.bottom + 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Health Metrics */}
        <View style={{ paddingHorizontal: 20, marginTop: 25 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Current Health Status
          </Text>

          <View
            style={{
              flexDirection: "row",
              flexWrap: "wrap",
              justifyContent: "space-between",
            }}
          >
            {healthMetrics.map((metric, index) => (
              <View
                key={index}
                style={{
                  width: "48%",
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
                <metric.icon size={28} color={metric.color} />
                <Text
                  style={{
                    fontSize: 18,
                    fontWeight: "bold",
                    color: "#2C3E50",
                    marginTop: 10,
                  }}
                >
                  {metric.value}
                </Text>
                <Text
                  style={{
                    fontSize: 14,
                    color: "#7F8C8D",
                    marginTop: 2,
                  }}
                >
                  {metric.title}
                </Text>
                <Text
                  style={{
                    fontSize: 12,
                    color: metric.color,
                    marginTop: 5,
                    fontWeight: "600",
                  }}
                >
                  {metric.status}
                </Text>
              </View>
            ))}
          </View>
        </View>

        {/* Upload Documents */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Documents
          </Text>

          <TouchableOpacity
            onPress={handleFileUpload}
            disabled={loading}
            style={{
              backgroundColor: "#E8F5E8",
              borderRadius: 15,
              padding: 20,
              flexDirection: "row",
              alignItems: "center",
              marginBottom: 20,
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.05,
              shadowRadius: 4,
              elevation: 2,
            }}
          >
            <Upload size={24} color="#27AE60" />
            <Text
              style={{
                fontSize: 16,
                fontWeight: "600",
                color: "#27AE60",
                marginLeft: 10,
                flex: 1,
              }}
            >
              {loading ? "Uploading..." : "Upload Medical Document"}
            </Text>
            <Plus size={20} color="#27AE60" />
          </TouchableOpacity>

          {/* Uploaded Files */}
          {uploadedFiles.map((file) => (
            <View
              key={file.id}
              style={{
                backgroundColor: "white",
                borderRadius: 15,
                padding: 15,
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
              <FileText size={20} color="#3498DB" />
              <View style={{ flex: 1, marginLeft: 10 }}>
                <Text
                  style={{
                    fontSize: 16,
                    fontWeight: "600",
                    color: "#2C3E50",
                  }}
                >
                  {file.name}
                </Text>
                <Text
                  style={{
                    fontSize: 14,
                    color: "#7F8C8D",
                  }}
                >
                  {file.type} • {file.date} • {file.size}
                </Text>
              </View>
            </View>
          ))}
        </View>

        {/* Recent Records */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Recent Medical Records
          </Text>

          {recentRecords.map((record) => (
            <TouchableOpacity
              key={record.id}
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
                <FileText size={20} color="#3498DB" />
                <Text
                  style={{
                    fontSize: 18,
                    fontWeight: "bold",
                    color: "#2C3E50",
                    marginLeft: 10,
                    flex: 1,
                  }}
                >
                  {record.title}
                </Text>
              </View>

              <View
                style={{
                  flexDirection: "row",
                  alignItems: "center",
                  marginBottom: 5,
                }}
              >
                <Text
                  style={{
                    fontSize: 14,
                    color: "#7F8C8D",
                    flex: 1,
                  }}
                >
                  Doctor: {record.doctor}
                </Text>
                <View
                  style={{
                    backgroundColor: "#F0F8FF",
                    paddingHorizontal: 8,
                    paddingVertical: 4,
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
                    {record.type}
                  </Text>
                </View>
              </View>

              <View style={{ flexDirection: "row", alignItems: "center" }}>
                <Calendar size={16} color="#7F8C8D" />
                <Text
                  style={{
                    fontSize: 14,
                    color: "#7F8C8D",
                    marginLeft: 5,
                  }}
                >
                  {record.date}
                </Text>
              </View>
            </TouchableOpacity>
          ))}
        </View>

        {/* Medications */}
        <View style={{ paddingHorizontal: 20, marginTop: 20 }}>
          <Text
            style={{
              fontSize: 22,
              fontWeight: "bold",
              color: "#8B4513",
              marginBottom: 15,
            }}
          >
            Current Medications
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
              <Pill size={20} color="#E74C3C" />
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: "600",
                  color: "#2C3E50",
                  marginLeft: 10,
                }}
              >
                Lisinopril 10mg
              </Text>
            </View>
            <Text
              style={{
                fontSize: 14,
                color: "#7F8C8D",
                marginBottom: 10,
              }}
            >
              Take once daily with breakfast
            </Text>
            <Text
              style={{
                fontSize: 12,
                color: "#27AE60",
                fontWeight: "600",
              }}
            >
              Next dose: Tomorrow 8:00 AM
            </Text>
          </View>
        </View>
      </ScrollView>
    </View>
  );
}
