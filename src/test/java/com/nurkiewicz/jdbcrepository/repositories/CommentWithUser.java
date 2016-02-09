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
package com.nurkiewicz.jdbcrepository.repositories;

import org.springframework.data.domain.Persistable;

import java.util.Date;

public class CommentWithUser extends Comment implements Persistable<Integer> {

    private User user;

    public CommentWithUser(User user, String contents, Date createdTime, int favouriteCount) {
        super(user.getUserName(), contents, createdTime, favouriteCount);
        this.user = user;
    }

    public CommentWithUser(Integer id, User user, String contents, Date createdTime, int favouriteCount) {
        super(id, user.getUserName(), contents, createdTime, favouriteCount);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentWithUser)) return false;
        if (!super.equals(o)) return false;

        CommentWithUser that = (CommentWithUser) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CommentWithUser(id=" + getId() + ", user=" + user +  ", contents='" + getContents() + '\'' + ", createdTime=" + getCreatedTime() + ", favouriteCount=" + getFavouriteCount() + ')';
    }

}
