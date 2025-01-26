/*
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package labs.pm.app;

import labs.pm.data.Product;

import java.math.BigDecimal;


/**
 * {@code Shop} class represents an application that manages Products
 * @version 1.0
 * @author AymanElMikh
 */
public class Shop {
    public static void main(String[] args) {

        Product p1 = new Product();

        p1.setId(1);
        p1.setName("Shoes");
        p1.setPrice(BigDecimal.valueOf(121.211));

        System.out.println(p1);
        System.out.println(p1.getDiscount());
    }

}