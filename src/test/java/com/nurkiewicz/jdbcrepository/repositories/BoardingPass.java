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

import static com.nurkiewicz.jdbcrepository.JdbcRepository.pk;

public class BoardingPass implements Persistable<Object[]> {

    private transient boolean persisted;

    private String flightNo;

    private int seqNo;

    private String passenger;

    private String seat;

    public BoardingPass() {
    }

    public BoardingPass(String flightNo, int seqNo, String passenger, String seat) {
        this.flightNo = flightNo;
        this.seqNo = seqNo;
        this.passenger = passenger;
        this.seat = seat;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public String getPassenger() {
        return passenger;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    @Override
    public Object[] getId() {
        return pk(flightNo, seqNo);
    }

    @Override
    public boolean isNew() {
        return !persisted;
    }

    public BoardingPass withPersisted(boolean persisted) {
        this.persisted = persisted;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardingPass)) return false;

        BoardingPass that = (BoardingPass) o;

        if (seqNo != that.seqNo) return false;
        if (flightNo != null ? !flightNo.equals(that.flightNo) : that.flightNo != null) return false;
        if (passenger != null ? !passenger.equals(that.passenger) : that.passenger != null) return false;
        return !(seat != null ? !seat.equals(that.seat) : that.seat != null);

    }

    @Override
    public int hashCode() {
        int result = flightNo != null ? flightNo.hashCode() : 0;
        result = 31 * result + seqNo;
        result = 31 * result + (passenger != null ? passenger.hashCode() : 0);
        result = 31 * result + (seat != null ? seat.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BoardingPass{flightNo='" + flightNo + '\'' + ", seqNo=" + seqNo + ", passenger='" + passenger + '\'' + ", seat='" + seat + '\'' + '}';
    }
}
