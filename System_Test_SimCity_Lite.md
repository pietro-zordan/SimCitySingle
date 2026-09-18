# Documento di System Test

**Progetto:** SimCity Lite  
**Contenuto:** 12 user story e 73 acceptance criteria  

## User Story 01

> **User story:** As a player, I want to choose an entity and place it on the grid, so that I can develop the city.
> **Description:** Houses, factories, shops, parks, roads, and power plants must be available.

| Acceptance criterion                                                                                                                                     | Validazione | Data  |
| -------------------------------------------------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US01.AC01** An entity can only be placed inside the grid and on an empty cell.                                                                         |     ok      | 12/08 |
| **US01.AC02** A cell can contain at most one entity.                                                                                                     |     ok      | 13/08 |
| **US01.AC03** The cost is immediately deducted from the budget: house 200, factory 600, shop 300, park 150, and power plant 800.                         |     ok      | 15/08 |
| **US01.AC04** Roads must be adjacent to other roads to be built.                                                                                         |     ok      | 15/08 |
| **US01.AC05** Buildings different than road must adjacent to a road to be placed                                                                         |     ok      | 14/08 |
| **US01.AC06** If the budget is insufficient, the placement is rejected without modifying the city or the budget.                                         |     ok      | 13/08 |
| **US01.AC07** If the cell is already occupied, nothing happens.                                                                                          |     ok      | 12/08 |
| **US01.AC08** A minimum number of citizens is required to place shops and factories.                                                                     |     ok      | 13/08 |
| **US01.AC09** If the placement does not throw any exceptions, the dashboard must display the cells colored with the color of the corresponding building. |     ok      | 14/08 |
| **US01.AC10** Technical constraint: the creation of the different entities should be handled using the Factory Pattern.                                  |     ok      | 15/08 |

## User Story 02

> **User story:** As a player, I want to advance to the next turn, so that I can observe the evolution of the city.
> **Description:** At each tick, the observer visits the entities present and communicates any changes to the main program.

| Acceptance criterion                                                                                           | Validazione | Data  |
| -------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US02.AC01** The "Next Turn" command increases the tick counter by exactly 1.                                 |     ok      | 06/08 |
| **US02.AC02** Each existing entity is processed exactly once.                                                  |     ok      | 06/08 |
| **US02.AC03** Changes to global variables correspond to the sum of the local effects.                          |     ok      | 07/08 |
| **US02.AC04** The budget on the next turn = budget + economy.                                                  |     ok      | 07/08 |
| **US02.AC05** Modifiers due to policies and events are applied before the player can advance to the next turn. |     ok      | 15/08 |
| **US02.AC06** Once the calculation is complete, the dashboard displays the new values.                         |     ok      | 18/08 |

## User Story 03

> **User story:** As a player, I want to choose an urban policy so that I can adopt different development strategies.
> **Description:** At least an Environmental Policy and an Industrial Policy must be available, implemented using the Strategy Pattern.

| Acceptance criterion                                                                                                                         | Validazione | Data  |
| -------------------------------------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US03.AC01** Only one policy can be active at a time.                                                                                       |     ok      | 15/08 |
| **US03.AC02** The Environmental Policy increases the cost of factories and reduces the cost of parks by the configured values.               |     ok      | 16/08 |
| **US03.AC03** The Industrial Policy reduces the cost of factories and increases the cost of parks by the configured values.                  |     ok      | 15/08 |
| **US03.AC04** The policy affects only subsequent placements, without charging for or refunding buildings that have already been constructed. |     ok      | 17/08 |
| **US03.AC05** The player can change the policy only after 12 ticks.                                                  |     ok      | 15/08 |
| **US03.AC06** The active policy also modifies the calculation of pollution or income.                                                        |     ok      | 16/08 |
| **US03.AC07** The dashboard always displays the active policy.                                                                               |     ok      | 20/08 |

## User Story 04

> **User story:** As a player, I want to save a city and resume the simulation later so that I do not lose my progress.
> **Description:** Persistence must use a JSON file, without a database.

| Acceptance criterion                                                                                                  | Validazione | Data  |
| --------------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US04.AC01** The save file contains the grid, buildings, coordinates, statistics, ticks, policies, and event status. |     ok      | 27/08 |
| **US04.AC02** After loading the file, the city must match the saved one.                                              |     ok      | 27/08 |
| **US04.AC03** A non-existent or invalid file produces an error message.                                               |     ok      | 27/08 |
| **US04.AC04** A failed load does not modify the currently open city.                                                  |     ok      | 27/08 |

## User Story 05

> **User story:** As a player, I want a graphical user interface that allows me to interact with and view the evolving city.
> **Description:** The following elements must be available: various buttons for interacting with objects and saving/loading the game, a grid for placing buildings, one or more charts for statistics, and a display panel showing the values of key city variables.

| Acceptance criterion                                                                                                                            | Validazione | Data  |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US05.AC01** Technical constraint: The Observer pattern must be used to notify the main component of updates from the class managing the city. |     ok      | 20/08 |
| **US05.AC02** Charts must display changes in their respective variables in real time.                                                           |     ok      | 24/08 |
| **US05.AC03** The grid must display the addition of new buildings in real time.                                                                 |     ok      | 24/08 |
| **US05.AC04** The grid must display the effects of currently active events.                                                                     |     ok      | 24/08 |
| **US05.AC05** Any exceptions or errors must be displayed.                                                                                       |     ok      | 24/08 |
| **US05.AC06** There must be a button to save the game.                                                                                          |     ok      | 26/08 |
| **US05.AC07** There must be a button to load a saved game.                                                                                      |     ok      | 26/08 |
| **US05.AC08** Technical constraint: Use JavaFX for the graphical user interface.                                                                |     ok      | 26/08 |

## User Story 06

> **User story:** As a player, I want power plants to supply nearby buildings with energy, so that urban areas can operate and grow.
> **Description:** An active power plant supplies the 49 cells surrounding it as long as it has enough energy available to provide.

| Acceptance criterion                                                                                                                  | Validazione | Data  |
| ------------------------------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US06.AC01** A building in one of the 49 cells adjacent to an active power plant is powered (if there is enough energy to power it). |     ok      | 10/08 |
| **US06.AC02** A building more than 3 cells away, even diagonally, is not powered by that power plant.                                 |     ok      | 10/08 |
| **US06.AC03** At the edges of the grid, only existing cells are considered.                                                           |     ok      | 10/08 |
| **US06.AC04** A destroyed or temporarily deactivated power plant does not provide energy.                                             |     ok      | 10/08 |
| **US06.AC05** The power plant's energy balance takes into account the consumption of each building.                                   |     ok      | 10/08 |

## User Story 07

> **User story:** As a player, I want buildings to grow gradually and only under the correct conditions, so that I can have realistic urban development.
> **Description:** Growth occurs for all buildings that are in conditions that allow them to grow (only houses, factories, and shops are involved).

| Acceptance criterion                                                                                      | Validazione | Data  |
| --------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US07.AC01** Any possible growth is evaluated at each tick.                                              |     ok      | 21/08 |
| **US07.AC02** A house/factory/shop can only grow if it is powered by an adjacent functioning power plant. |     ok      | 21/08 |
| **US07.AC03** Every new house starts with 10 inhabitants and then grows gradually up to 50.               |     ok      | 21/08 |
| **US07.AC04** The global population corresponds to the sum of the population of the existing houses.      |     ok      | 21/08 |
| **US07.AC05** If there is no functioning power plant, houses lose population until they disappear.        |     ok      | 21/08 |
| **US07.AC06** If there are no houses, the population cannot fall below 0.                                 |     ok      | 21/08 |

## User Story 08

> **User story:** As a player, I want a fire to start and spread between adjacent buildings.

| Acceptance criterion                                                            | Validazione | Data  |
| ------------------------------------------------------------------------------- | :---------: | :---: |
| **US08.AC01** The fire starts in a randomly selected house.                     |     ok      | 22/08 |
| **US08.AC02** The initial house is destroyed.                                   |     ok      | 22/08 |
| **US08.AC03** The fire spreads to the 4 adjacent cells with a probability 50%.  |     ok      | 22/08 |
| **US08.AC04** Each affected building is removed.                                |     ok      | 22/08 |
| **US08.AC05** The propagation does not exceed the grid boundaries.              |     ok      | 22/08 |
| **US08.AC06** Global statistics are recalculated in real time during the event. |     ok      | 22/08 |
| **US08.AC07** Cells affected by the fire are colored red.                       |     ok      | 26/08 |

## User Story 09

> **User story:** As a player, I want an energy crisis to occur, so that I have to pay higher operating costs.

| Acceptance criterion                                                                                                | Validazione | Data  |
| ------------------------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US09.AC01** The crisis lasts for 5 ticks.                                                           |     ok      | 23/08 |
| **US09.AC02** During the event, the budget decreases proportionally to the number of buildings present on the grid. |     ok      | 23/08 |
| **US09.AC03** At the end of the event, the budget automatically returns to normal values.                           |     ok      | 23/08 |

## User Story 10

> **User story:** As a player, I want an economic boom to occur, so that I can temporarily benefit from faster production growth.

| Acceptance criterion                                                                       | Validazione | Data  |
| ------------------------------------------------------------------------------------------ | :---------: | :---: |
| **US10.AC01** The boom lasts for 5 ticks.                                    |     ok      | 23/08 |
| **US10.AC02** During the event, factories and shops grow with a randomic multiplier of rage 0.3-0.8. |     ok      | 23/08 |
| **US10.AC03** Houses, parks, roads, and power plants do not receive the bonus.             |     ok      | 23/08 |
| **US10.AC04** The bonus modifies the growth speed, not the placement costs.                |     ok      | 23/08 |
| **US10.AC05** At the end of the event, normal speed is restored.                           |     ok      | 23/08 |
| **US10.AC06** If no factories or shops exist, the event does not trigger any errors.       |     ok      | 23/08 |

## User Story 11

> **User story:** As a developer, I want a hacker attack to be able to disable a power plant, so that the player is forced to endure a loss of electricity.

| Acceptance criterion                                                                              | Validazione | Data  |
| ------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US11.AC01** The event randomly selects an existing, functional power plant.                     |     ok      | 22/08 |
| **US11.AC02** The power plant remains disabled for 5 ticks.                         |     ok      | 22/08 |
| **US11.AC03** During the lockout, the power plant neither produces nor distributes energy.        |     ok      | 26/08 |
| **US11.AC04** Buildings without other adjacent power plants become unpowered.                     |     ok      | 22/08 |
| **US11.AC05** Once the duration expires, the power plant automatically becomes operational again. |     ok      | 22/08 |
| **US11.AC06** If no power plants exist, the event ends without modifying the city.                |     ok      | 22/08 |

## User Story 12

> **User story:** As a player, I want a tsunami to occur so that coastal areas face destruction and city happiness decreases.

| Acceptance criterion                                                                                | Validazione | Data  |
| --------------------------------------------------------------------------------------------------- | :---------: | :---: |
| **US12.AC01** The tsunami approaches from a randomly selected direction (UP, DOWN, LEFT, or RIGHT). |     ok      | 23/08 |
| **US12.AC02** The wave advances inward up to 4 cells deep from the selected side of the grid.       |     ok      | 23/08 |
| **US12.AC03** Buildings within the affected area are removed, excluding roads and parks.            |     ok      | 23/08 |
| **US12.AC04** City statistics are refreshed immediately upon the tsunami's arrival.                 |     ok      | 23/08 |
| **US12.AC05** Global happiness decreases by 50*tick points on each tick during the event.                 |     ok      | 23/08 |
