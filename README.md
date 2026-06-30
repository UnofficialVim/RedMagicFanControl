# RedMagic Fan Control

A systemless fan control solution for RedMagic devices using a Magisk module.

## Overview

RedMagic Fan Control is a Magisk-based project that provides control over the built-in cooling fan on supported RedMagic phones.

The project is split into two parts:

- **Magisk Module** — handles root access, hardware communication, and fan control
- **Android App** — provides a user interface for controlling and monitoring the module (planned)

## Features

Currently:

- Magisk module structure
- Boot-time daemon
- Fan hardware detection foundation
- Config-based design

Planned:

- Fan status monitoring
- Fan speed control
- Call-based fan behavior
- Local socket API
- Native Android companion app

## Supported Devices

Target device:
- RedMagic 9 pro

Support depends on whether the device exposes compatible fan controls.

