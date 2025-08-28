    public void run() {
        // check to see if the event log is hooked up
        //
        if (m_eventLog == null)
            return;

        // open up a logger
        //
        Category log = ThreadCategory.getInstance(getClass());

        Events events = m_eventLog.getEvents();
        if (events == null || events.getEventCount() <= 0) {
            // no events to process
            return;
        }

        // create an EventWriters
        EventWriter eventWriter = null;
        AlarmWriter alarmWriter = null;
        try {
            eventWriter = new EventWriter(m_getNextEventIdStr);
            alarmWriter = new AlarmWriter(m_getNextAlarmIdStr);
        } catch (Throwable t) {
            log.warn("Exception creating EventWriter", t);
            log.warn("Event(s) CANNOT be inserted into the database");

            return;
        }

        Enumeration en = events.enumerateEvent();
        while (en.hasMoreElements()) {
            Event event = (Event) en.nextElement();

            if (log.isDebugEnabled()) {
                // print out the eui, source, and other
                // important aspects
                //
                String uuid = event.getUuid();
                log.debug("Event {");
                log.debug("  uuid  = " + (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                log.debug("  uei   = " + event.getUei());
                log.debug("  src   = " + event.getSource());
                log.debug("  iface = " + event.getInterface());
                log.debug("  time  = " + event.getTime());
                Parm[] parms = (event.getParms() == null ? null : event.getParms().getParm());
                if (parms != null) {
                    log.debug("  parms {");
                    for (int x = 0; x < parms.length; x++) {
                        if ((parms[x].getParmName() != null) && (parms[x].getValue().getContent() != null)) {
                            log.debug("    (" + parms[x].getParmName().trim() + ", " + parms[x].getValue().getContent().trim() + ")");
                        }
                    }
                    log.debug("  }");
                }
                log.debug("}");
            }

            // look up eventconf match and expand event
            EventExpander.expandEvent(event);
            try {
                // add to database
                eventWriter.persistEvent(m_eventLog.getHeader(), event);
                // send event to interested listeners
                EventIpcManagerFactory.getIpcManager().broadcastNow(event);
            } catch (SQLException sqle) {
                log.warn("Unable to add event to database", sqle);
            } catch (Throwable t) {
                log.warn("Unknown exception processing event", t);
            }
            try {
                alarmWriter.persistAlarm(m_eventLog.getHeader(), event);
            } catch (SQLException sqle) {
                log.warn("Unable to add alarm to database", sqle);
            }

        }

        // close database related stuff in the eventwriter
        eventWriter.close();
        alarmWriter.close();
    }
