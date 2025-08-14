[![Maven Package](https://github.com/Gemesil/FasterPathways/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/Gemesil/FasterPathways/actions/workflows/maven-publish.yml)
![Java](https://img.shields.io/badge/dynamic/xml?color=orange&label=Java&query=%2F%2F%2A%5Blocal-name%28%29%3D%27java.version%27%5D&url=https%3A%2F%2Fraw.githubusercontent.com%2FHorizonik%2FFasterPathways%2Fmain%2Fpom.xml)
![Spigot](https://img.shields.io/badge/Spigot-1.21.8-yellow)

<p align="center">
  <img src="icons/icon.png" width="200">
</p>

# FasterPathways Plugin
**FasterPathways** is a Minecraft plugin that enhances travel by granting speed effects when walking on configurable blocks. Create efficient transportation networks in your Minecraft world using any blocks you choose. This plugin aims to make your journeys more enjoyable and efficient.

## Features
  - Configurable speed blocks (not just dirt paths)
  - Adjustable speed effect level and duration
  - World-specific enabling/disabling
  - Customizable action bar messages with color codes
  - Multi-block support for creating varied path networks

## Installation
  - Compatible with Spigot/Paper 1.19+
  - Download plugin JAR
  - Place in plugins folder
  - Restart server

## Configuration
The Faster Pathways plugin provides a configuration file (**'config.yml'**) that allows you to adjust various settings. Here's an explanation of each adjustable setting:
    
    # Speed effect settings
    speed:
     # Set this to the level of the speed effect you'd like to be applied when a player steps on a dirt_path. Default is 2.
      level: 3
      # The amount of time (in seconds) that the speed effect will be applied after stepping on a dirt path. Default is 1.
      duration: 1
    
    # Messages
    messages:
     # The message that will be displayed in a player's hot bar while walking on a dirt path block (supports color codes with &)
      actionBar: "&eTravel blocks are causing you to move faster!"
      # Whether to show the action bar message
      showActionBar: true
    
    # List of blocks that will give speed effect
    # Use Bukkit material names (see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)
    speedBlocks:
     - DIRT_PATH
    
    # Worlds where the speed effect is disabled, you can specify "disabledWorlds: []" to allow it on all worlds
    disabledWorlds:
     - example_world

## Usage
 Walk on any configured block type to gain a speed boost. Create paths using any combination of configured blocks.

## Feedback and Support
If you encounter any issues or have suggestions for improving Faster Pathways, please visit the ![Spigot Page](https://www.spigotmc.org/resources/fasterpathways.109889/) to leave your feedback.

## License
Faster Pathways is licensed under the ![MIT License](https://opensource.org/license/mit/).

*Enjoy your enhanced travel experience with Faster Pathways!*
