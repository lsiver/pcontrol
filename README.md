# Process Control Program Description

## Overview

This program is a desktop process-control simulator written in Java. It lets a user:

- define a simple process model using `Kp`, `tau`, and `deadtime`
- view the process's open-loop response to a fixed manual step
- configure a PID controller and simulate closed-loop behavior
- compare setpoint-response behavior and disturbance-rejection behavior
- save and reload tuning cases from disk

The application uses JavaFX for the main interface and embeds JFreeChart charts for the control plots.

## Main Purpose

The program is designed to help someone explore how a first-order-plus-deadtime style process responds both:

- without feedback control
- with PID feedback control

It is effectively a small teaching and tuning tool for process control. The user can change process parameters and controller parameters, then immediately see how those changes affect:

- process variable (`PV`)
- setpoint (`SP`)
- controller output (`OP`)

## How the Application Starts

The program starts from `AppLauncher`, which launches the JavaFX `Main` application class.

When the main window opens, it:

- creates a tabbed interface
- builds all three tabs
- immediately calculates and displays the open-loop plot
- immediately calculates and displays the control plots

## User Interface Structure

The window contains three tabs:

1. `Open Loop`
2. `Control`
3. `Library`

Each tab serves a different part of the workflow.

## Open Loop Tab

The `Open Loop` tab is used to define the process model and view its natural response to a manual output step.

### Inputs

- `Horizon`: total simulation time
- `Kp`: process gain
- `tau`: process time constant
- `deadtime`: transport delay before the process reacts

### What happens when Replot is clicked

The program:

- validates the numeric inputs
- builds a `Process` object using those values
- creates a `Simulation` object
- runs `runOpenLoopSim()`
- draws a line chart called `Process Response`

### Open-loop model behavior

The open-loop simulation applies a fixed manual controller output of `1.0`. That step is delayed by the configured deadtime and then sent through a first-order process model.

The process variable is updated each time step using:

`PV change = (dt / tau) * (Kp * delayed OP - current PV)`

This means the tab is showing how the modeled process responds by itself to a unit step in output.

## Control Tab

The `Control` tab uses the process values from the `Open Loop` tab and adds a PID controller on top of that process.

### Controller inputs

- `Kc`: controller gain
- `Ti`: integral time
- `Td`: derivative time
- `Equation`: controller form `A`, `B`, or `C`

### Range and limit inputs

- `PV Hi`
- `PV Lo`
- `OP Hi`
- `OP Lo`

These values are used to normalize error and process changes and to clamp the controller output to a permitted range.

### Scenario inputs

- `dSP`: size of the setpoint step
- `dOP`: disturbance/load step used in the disturbance simulation

### Outputs shown on this tab

The tab produces two separate charts:

- `Setpoint Change Response`
- `Disturbance Response`

Both charts display:

- `PV`
- `SP`
- `OP`

The `PV` and `SP` traces share the left axis. `OP` is drawn on a second axis on the right.

## PID Logic Implemented by the Program

The simulator supports three controller equation variants:

- Honeywell EQN `A`
- Honeywell EQN `B`
- Honeywell EQN `C`

All three use incremental output updates in `runSPChangeSim()` and `runDisturbanceSim()`.

### Shared behavior

- error is normalized by PV span: `abs(PV Hi - PV Lo)`
- output changes are scaled by OP span: `abs(OP Hi - OP Lo)`
- integral action is always based on error
- output is clamped between `OP Lo` and `OP Hi`

### Equation type differences

`A`

- proportional action uses change in error
- derivative action uses change in error

`B`

- proportional action uses change in error
- derivative action uses change in PV

`C`

- proportional action uses change in PV
- derivative action uses change in PV

This allows the user to compare different practical PID structures without changing the process model.

## Setpoint-Change Simulation

The setpoint simulation:

- starts from `PV = 0`
- starts from `OP = 0`
- applies the requested setpoint step immediately
- calculates controller action at each time step
- passes the controller output through the deadtime buffer
- updates the process variable using the first-order process model

The final result is a set of arrays containing time-series data for:

- `PV`
- `SP`
- `OP`

These arrays are then sent to the plotting code.

## Disturbance-Rejection Simulation

The disturbance simulation is similar to the setpoint case but is intended to show how the controller reacts to a load disturbance.

It:

- initializes `OP` with the disturbance/load-step value
- keeps `SP = 0`
- feeds that value through the deadtime buffer
- lets the process move away from setpoint
- computes corrective PID action to drive the process back

This gives the user a separate view of regulatory control performance rather than servo performance.

## Simulation Resolution

The simulation time step is fixed in the UI code:

- `DEFAULT_DT = 0.05`

That value is used whenever a `Process` object is built from the input fields.


## Library Tab

The `Library` tab is a lightweight tuning manager.

It lets the user:

- choose a loop tag name
- load a tuning file from disk
- save the current settings as a tuning snapshot
- apply a selected saved tuning back into the input fields
- delete a saved tuning

### Saved data

Each saved tuning stores:

- process parameters
- PID parameters
- equation type
- PV limits
- OP limits
- setpoint step
- disturbance step

### File format

Saved runs are written to a file named:

`<loop tag>.tun`

Each line in that file contains one saved run. The fields are serialized with the `|` character as a separator.

### Applying a saved tuning

When a tuning is selected and applied, the program copies all saved values back into the UI fields and reruns the control plots.

Double-clicking a saved run also applies it immediately.


## What the Program Is Best For

This program is best understood as a quick PID tuning visualizer to aid in tunign loops.

It is useful for seeing how process dynamics and controller settings interact, especially for first-order systems with deadtime.

## Current Limitations

Based on the current implementation:

- the process model is limited to a simple first-order-plus-deadtime style response
- saved tunings are plain text records, not a structured database
- the program focuses on visualization and manual experimentation rather than automatic tuning

## Summary

In practical terms, this application is a small interactive simulator for process control. It lets a user define a process, tune a PID controller, observe both setpoint and disturbance responses, and store multiple tuning cases for later reuse.
