package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.model.Frame;

record PacketComponents(Class<? extends Frame> packet, Class<?>[] components){}