/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.main;

import com.example.collections.Contact;
import com.example.collections.Phone;
import com.example.collections.types.ContactType;
import com.example.collections.types.PhoneType;
import com.arquivolivre.mongocom.management.CollectionManager;
import com.arquivolivre.mongocom.management.CollectionManagerFactory;
import com.arquivolivre.mongocom.management.MongoQuery;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 */
public class Main {

    public static void main(String[] args) {
        // Connect to MongoDB using the framework's built-in configuration support
        // The framework automatically loads configuration from conf/database.properties (or .conf/.config)
        System.out.println("Loading MongoDB configuration from conf/database.properties...");
        CollectionManager cm = CollectionManagerFactory.setup();
        //create a document with my information
        Contact myself = new Contact("Thiago");
        Contact myCompany = new Contact("My Company");
        Contact companyOwner = new Contact("Other Company");
        myself.addPhone(new Phone(PhoneType.MOBILE, 55, 99, 9999999));
        myself.setType(ContactType.PERSON);
        myself.setEmail("thiago@sjrp.unesp.br");
        myCompany.setType(ContactType.COMPANY);
        myCompany.setCompany(companyOwner);
        companyOwner.setType(ContactType.COMPANY);
        myself.setCompany(myCompany);
        //insert a new document
        cm.insert(myself);
        //you can get te Id after a insertion
        System.out.println(myself.getId());

        //recover information from de database
        myself = cm.findOne(Contact.class, new MongoQuery("name", "Thiago"));
        if (myself != null) {
            System.out.println("Name: " + myself.getName());
            System.out.println("Email: " + myself.getEmail());
            for (Phone p : myself.getPhones()) {
                System.out.printf("%s: +%d (%d) %d\n", p.getPhoneType().toString(), p.getCountryCode(), p.getAreaCode(), p.getPhoneNumber());
            }
            System.out.println("Company: " + myself.getCompany().getName());
            System.out.println("Company Owner: " + myself.getCompany().getCompany().getName());
        }
    }
}
