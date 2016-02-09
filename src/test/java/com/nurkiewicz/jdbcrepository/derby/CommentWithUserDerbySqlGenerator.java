/*
 * Copyright 2012-2014 Tomasz Nurkiewicz <nurkiewicz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nurkiewicz.jdbcrepository.derby;

import com.nurkiewicz.jdbcrepository.sql.DerbySqlGenerator;

public class CommentWithUserDerbySqlGenerator extends DerbySqlGenerator {

    public CommentWithUserDerbySqlGenerator() {
        super("c.*, u.date_of_birth, u.reputation, u.enabled");
    }

}
