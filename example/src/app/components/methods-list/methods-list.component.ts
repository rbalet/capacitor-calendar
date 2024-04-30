import { Component, NgZone } from '@angular/core';
import { IonIcon, IonItem, IonLabel, IonList, IonListHeader, IonPicker } from '@ionic/angular/standalone';
import {
  CalendarChooserDisplayStyle,
  CalendarChooserSelectionStyle,
  CapacitorCalendar,
  PluginPermission,
  PluginPermissionsMap,
  ReminderRecurrenceFrequency,
} from '@ebarooni/capacitor-calendar';
import { StoreService } from '../../store/store.service';
import { calendarChooserPickerColumns } from '../../ion-picker-data/calendar-chooser/calendar-chooser-picker-columns';
import {
  getCalendarChooserPickerButtons,
  CalendarChooserResult,
} from '../../ion-picker-data/calendar-chooser/calendar-chooser-picker-buttons';
import { checkPermissionPickerColumns } from '../../ion-picker-data/check-permission/check-permission-picker-columns';
import {
  getCheckPermissionPickerButtons,
  CheckPermissionPickerResult,
} from '../../ion-picker-data/check-permission/check-permission-picker-buttons';
import { EventsListViewModalComponent } from '../events-list-view-modal/events-list-view-modal.component';

@Component({
  selector: 'app-methods-list',
  templateUrl: './methods-list.component.html',
  imports: [IonIcon, IonItem, IonLabel, IonList, IonListHeader, IonPicker, EventsListViewModalComponent],
  standalone: true,
})
export class MethodsListComponent {
  public readonly calendarChooserPickerColumns = calendarChooserPickerColumns;
  public readonly calendarChooserPickerButtons = getCalendarChooserPickerButtons((result: CalendarChooserResult) =>
    this.zone.run(() => this.selectCalendarsWithPrompt(result.selectionStyle.value, result.displayStyle.value))
  );
  public readonly checkPermissionPickerColumns = checkPermissionPickerColumns;
  public readonly checkPermissionPickerButtons = getCheckPermissionPickerButtons(
    (result: CheckPermissionPickerResult) => this.zone.run(() => this.checkPermission(result.alias.value))
  );
  public readonly requestPermissionPickerButtons = getCheckPermissionPickerButtons(
    (result: CheckPermissionPickerResult) => this.zone.run(() => this.requestPermission(result.alias.value))
  );

  constructor(
    private readonly storeService: StoreService,
    private readonly zone: NgZone
  ) {}

  public createEventWithPrompt(): void {
    const now = Date.now();
    CapacitorCalendar.createEventWithPrompt({
      title: 'Capacitor Calendar',
      startDate: now,
      endDate: now + 2 * 60 * 60 * 1000,
      location: 'Capacitor Calendar',
      isAllDay: false,
    })
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public selectCalendarsWithPrompt(
    selectionStyle: CalendarChooserSelectionStyle,
    displayStyle: CalendarChooserDisplayStyle
  ): void {
    CapacitorCalendar.selectCalendarsWithPrompt({ selectionStyle: selectionStyle, displayStyle: displayStyle })
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public checkAllPermissions(): void {
    CapacitorCalendar.checkAllPermissions()
      .then((response) => {
        this.storeService.updateState({ permissions: response });
        this.storeService.dispatchLog(JSON.stringify(response));
      })
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public requestAllPermissions(): void {
    CapacitorCalendar.requestAllPermissions()
      .then((response) => {
        this.storeService.updateState({ permissions: response });
        this.storeService.dispatchLog(JSON.stringify(response));
      })
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public listCalendars(): void {
    CapacitorCalendar.listCalendars()
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public getDefaultCalendar(): void {
    CapacitorCalendar.getDefaultCalendar()
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public checkPermission(alias: PluginPermission): void {
    CapacitorCalendar.checkPermission({ alias: alias })
      .then((response) => {
        const permissionState: Partial<PluginPermissionsMap> = {};
        permissionState[alias] = response.result;
        this.storeService.updateState({ permissions: permissionState });
        this.storeService.dispatchLog(JSON.stringify(response));
      })
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public createEvent(): void {
    const now = Date.now();
    CapacitorCalendar.createEvent({
      title: 'Capacitor Calendar',
      startDate: now,
      endDate: now + 2 * 60 * 60 * 1000,
      location: 'Capacitor Calendar',
      isAllDay: false,
    })
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public requestPermission(alias: PluginPermission): void {
    CapacitorCalendar.requestPermission({ alias: alias })
      .then((result) => {
        const update: Partial<PluginPermissionsMap> = {};
        switch (alias) {
          case PluginPermission.READ_CALENDAR:
            update[PluginPermission.READ_CALENDAR] = result.result;
            break;
          case PluginPermission.WRITE_CALENDAR:
            update[PluginPermission.WRITE_CALENDAR] = result.result;
            break;
          case PluginPermission.READ_REMINDERS:
            update[PluginPermission.READ_REMINDERS] = result.result;
            break;
          case PluginPermission.WRITE_REMINDERS:
            update[PluginPermission.WRITE_REMINDERS] = result.result;
            break;
        }
        this.storeService.updateState({ permissions: update });
        this.storeService.dispatchLog(JSON.stringify(result));
      })
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public getDefaultRemindersList(): void {
    CapacitorCalendar.getDefaultRemindersList()
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public getRemindersLists(): void {
    CapacitorCalendar.getRemindersLists()
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public createReminder(): void {
    CapacitorCalendar.createReminder({
      title: 'Capacitor Calendar',
      notes: 'A CapacitorJS Plugin',
      priority: 5,
      dueDate: Date.now(),
      isCompleted: false,
      url: 'https://capacitor-calendar.pages.dev/',
      location: 'Remote',
      recurrence: {
        frequency: ReminderRecurrenceFrequency.WEEKLY,
        interval: 3,
        end: Date.now() + 6 * 7 * 24 * 60 * 60 * 1000, // 6 weeks from now
      },
    })
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public openCalendar(): void {
    CapacitorCalendar.openCalendar({
      date: Date.now() + 24 * 60 * 60 * 1000, // tomorrow
    }).catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public openReminders(): void {
    CapacitorCalendar.openReminders().catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public listEventsInRange(): void {
    CapacitorCalendar.listEventsInRange({
      startDate: Date.now(),
      endDate: Date.now() + 6 * 7 * 24 * 60 * 60 * 1000, // 6 weeks from now
    })
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }

  public deleteEventsById(ids: string[]): void {
    CapacitorCalendar.deleteEventsById({
      ids: ids,
    })
      .then((response) => this.storeService.dispatchLog(JSON.stringify(response)))
      .catch((error) => this.storeService.dispatchLog(JSON.stringify(error)));
  }
}
