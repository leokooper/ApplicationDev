package ch.makery.address.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Leonchenko on 06.04.2018.
 */

@XmlRootElement(name = "persons")
public class PersonListWrapper {

    private List<Person> persons;

    @XmlElement(name = "person")
    public List<Person> getPersons(){
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }
}
