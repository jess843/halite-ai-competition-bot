"""
Welcome to your first Halite-II bot!

This bot's name is Settler. It's purpose is simple (don't expect it to win complex games :) ):
1. Initialize game
2. If a ship is not docked and there are unowned planets
2.a. Try to Dock in the planet if close enough
2.b If not, go towards the planet

Note: Please do not place print statements here as they are used to communicate with the Halite engine. If you need
to log anything use the logging module.
"""
import hlt
import logging

game = hlt.Game("Settler")
logging.info("Starting my Settler bot!")

while True:
    game_map = game.update_map()

    command_queue = []
    for ship in game_map.get_me().all_ships():
        if ship.docking_status != ship.DockingStatus.UNDOCKED:
            continue

        entities_by_distance = game_map.nearby_entities_by_distance(ship)
        nearest_planet = None
        for distance in sorted(entities_by_distance):
          nearest_planet = next((nearest_entity for nearest_entity in entities_by_distance[distance] if isinstance(nearest_entity, hlt.entity.Planet)), None)
          if nearest_planet:
            break     

        planets = game_map.all_planets()
        ships = game_map.get_me().all_ships()
        for current in range(0, len(ships)):
          ships[current].navigate_to(ship.closest_point_to(planets[current%len(planets)]), game_map, speed=hlt.constants.MAX_SPEED/2))

    break;
    game.send_command_queue(command_queue)
