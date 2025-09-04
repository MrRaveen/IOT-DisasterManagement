# IoT-Based Disaster Management System
## Demo link
## Images
![WhatsApp Image 2025-09-03 at 11 00 07_b564d54b](https://github.com/user-attachments/assets/4f889aa8-0587-4661-9dd4-7ff2da583a16)
![WhatsApp Image 2025-09-03 at 11 00 40_8998d7c4](https://github.com/user-attachments/assets/053450a2-d18f-4765-8d3f-14479243ddce)
![WhatsApp Image 2025-09-03 at 11 01 32_c692e251](https://github.com/user-attachments/assets/bd4718f3-8368-4901-af67-7f1b119643c4)
![WhatsApp Image 2025-09-03 at 11 01 52_b5bd0cb1](https://github.com/user-attachments/assets/04d6d67f-7834-4d48-ba03-baebae8f81be)
<img width="960" height="1280" alt="image" src="https://github.com/user-attachments/assets/74909ef1-988d-44d7-b8b5-c734a883baa5" />
## Project Overview
This project, developed as part of the Higher National Diploma in Software Engineering at the National Institute of Business Management, focuses on creating an IoT-based disaster management system for detecting floods and landslides in Sri Lanka. The system integrates IoT sensors, a backend server, and a mobile application to provide real-time alerts to vulnerable communities, addressing the gaps in timely disaster communication.

## Background
Sri Lanka faces frequent floods and landslides due to its tropical climate and geography. In 2023, over 100,000 people were affected by these disasters (ReliefWeb, 2023). Current warning systems, reliant on traditional media, often fail to reach rural communities in time, increasing vulnerability. This project leverages IoT technology to enable real-time environmental monitoring and direct alerts via a mobile application.

## Problem Statement
- **Existing Issues**:
  - Unreliable detection methods in vulnerable areas.
  - Inadequate communication infrastructure for timely warnings.
  - Limited access to traditional media in remote regions.
- **Identified Gaps**:
  - Lack of localized IoT-based disaster management systems in Sri Lanka.
  - Limited historical data for predictive analysis.
  - Challenges in integrating residents into official communication channels.

## Objectives
### General Objective
To develop an IoT-based system for detecting floods and landslides and delivering real-time alerts to residents.

### Specific Objectives
- Design and implement a prototype using IoT sensors.
- Develop a mobile application for timely alerts.
- Create a backend system for processing and storing disaster-related data.
- Test the system’s effectiveness under simulated conditions.

## System Architecture
The system consists of:
- **IoT Sensors**: Monitor water levels, soil moisture, ground vibrations, and tilt.
- **Backend Server**: Processes sensor data using NodeJS and a REST API, with Firebase for storage.
- **Mobile Application**: Built with Android Studio, featuring a dashboard, push notifications, and settings for flood and landslide alerts.

### Hardware Components
- Water Level Sensor (MD0207)
- Soil Moisture Sensor
- MPU-6050 Accelerometer and Gyroscope
- SW520D Tilt Sensor

### Software Components
- **Backend**: NodeJS with REST API
- **Database**: Firebase
- **Mobile App**: Android Studio with Firebase Cloud Messaging (FCM) for notifications

## Implementation
- **Prototype**: Built using Arduino microcontrollers with sensors, tested in simulated flood and landslide environments.
- **Mobile App**: Features a dashboard, push notifications, and alert history.
- **Integration**: Sensors communicate data via Wi-Fi to the server, which triggers notifications through FCM.

## Testing and Results
- **Flood Detection**: Water level sensors triggered alerts accurately.
- **Landslide Detection**: Soil moisture and tilt sensors responded to changes.
- **Mobile App**: Alerts delivered within 3–5 seconds.
- **Limitations**:
  - Sensor accuracy affected by noise.
  - Occasional delays due to network issues.
  - Prototype conditions did not fully replicate real-world scenarios.

## Future Work
- Integrate with government disaster management systems.
- Use industrial-grade sensors for durability.
- Incorporate AI for predictive analytics.
- Expand to include tsunamis and earthquakes.

## Installation
1. **Hardware Setup**:
   - Connect the sensors (Water Level, Soil Moisture, MPU-6050, SW520D) to an Arduino microcontroller.
   - Ensure Wi-Fi connectivity for data transmission.
2. **Backend Setup**:
   - Install NodeJS and required dependencies.
   - Configure Firebase for data storage and FCM for notifications.
   - Run the REST API server.
3. **Mobile App**:
   - Clone the repository and open the project in Android Studio.
   - Build and deploy the app to an Android device.
4. **Testing**:
   - Simulate flood and landslide conditions to verify sensor and app performance.

## Usage
1. Deploy sensors in vulnerable areas to monitor environmental conditions.
2. Run the backend server to process sensor data.
3. Use the mobile app to receive real-time alerts and view disaster status.

## References
- ReliefWeb (2023) *Floods and landslides – Sri Lanka: Situation Report.* [https://reliefweb.int](https://reliefweb.int)
- National Building Research Organization (NBRO) (2023) *Damage assessment of recent landslide events in Sri Lanka.* [https://www.nbro.gov.lk](https://www.nbro.gov.lk)
- Tronic.lk (2023) *Electronic components datasheets.* [https://tronic.lk](https://tronic.lk)
- Ali, A., Ahmad, S., & Khan, M. (2022) ‘IoT-based flood monitoring and early warning system’, *International Journal of Disaster Risk Reduction*, 76, pp. 102–115.
- Kumar, R., & Singh, P. (2021) ‘Landslide detection using IoT sensors: A review’, *Journal of Geoscience and Environment Protection*, 9(4), pp. 55–70.
- Wijesinghe, K., & Perera, T. (2020) ‘Disaster management challenges in Sri Lanka: The role of ICT’, *Asian Journal of Disaster Risk Management*, 12(3), pp. 215–229.

## License
This project is submitted in partial fulfillment of the Higher National Diploma in Software Engineering at the National Institute of Business Management. Consent is given for photocopying and interlibrary loans, with the title and summary available to outside organizations.
