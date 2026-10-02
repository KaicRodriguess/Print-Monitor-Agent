package br.com.printmonitor.agent;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Main {



    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Use: Main <ip> <community>");
            return;
        }

        String ip = args[0];
        String community = args[1];

        // 1. Canal UDP para enviar e receber
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        //2. Alvo : Impressora
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(ip +"/161"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(2000);
        target.setRetries(1);

        //3. sysDescr (descrição do equipamento)
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(new OID("1.3.6.1.2.1.1.1.0"))));
        pdu.setType(PDU.GET);

        //4. Enviar e ler a resposta
        ResponseEvent<UdpAddress> event = snmp.send(pdu, target);
        PDU response = event.getResponse();

        if(response == null)
        {
            System.out.println("No response from printer");
        }
        else
        {
            System.out.println("Description : " + response.get(0).getVariable());
        }

        String description = readValue(snmp, target, "1.3.6.1.2.1.1.1.0");
        System.out.println("Description : " + description);

        String pages = readValue(snmp, target, "1.3.6.1.2.1.43.10.2.1.4.1.1");
        System.out.println("Páginas impressas : " + pages);



    }

    private static String readValue(Snmp snmp, CommunityTarget<UdpAddress> target, String oid) throws Exception
    {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        ResponseEvent<UdpAddress> event = snmp.send(pdu, target);
        PDU response = event.getResponse();

        if (response == null) {
            return null;                     // sem resposta
        }
        return response.get(0).getVariable().toString();

    }



}
