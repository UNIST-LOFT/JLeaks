/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.examples;

import uk.co.real_logic.sbe.generation.java.DirectBuffer;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class SbeExample
{
    private static final String ENCODING_FILENAME = "sbe.encoding.filename";
    private static final byte[] VEHICLE_CODE = {'a', 'b', 'c', 'd', 'e', 'f'};
    private static final byte[] MANUFACTURER_CODE = {'1', '2', '3'};
    private static final byte[] MAKE;
    private static final byte[] MODEL;

    private static final MessageHeader MESSAGE_HEADER = new MessageHeader();
    private static final Car CAR = new Car();

    static
    {
        try
        {
            MAKE = "Honda".getBytes("UTF-8");
            MODEL = "Civic VTi".getBytes("UTF-8");
        }
        catch (final UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static void main(final String[] args)throws Exception
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        final DirectBuffer directBuffer = new DirectBuffer(byteBuffer);
        final int messageTemplateVersion = 0;
        int bufferOffset = 0;
        int encodingLength = 0;

        // Setup for encoding a message

        MESSAGE_HEADER
            .reset(directBuffer, bufferOffset, messageTemplateVersion)
            .blockLength(CAR.blockLength())
            .templateId((int)CAR.templateId())
            .version((short)CAR.templateVersion());

        bufferOffset += MESSAGE_HEADER.size();
        encodingLength = MESSAGE_HEADER.size();
        encodingLength += encode(CAR, directBuffer, bufferOffset);

        // Optionally write the encoded buffer to a file for decoding by the On-The-Fly decoder

        final String encodingFilename = System.getProperty(ENCODING_FILENAME);
        if (encodingFilename != null)
        {
            FileChannel channel = new FileOutputStream(encodingFilename).getChannel();

            byteBuffer.limit(encodingLength);
            channel.write(byteBuffer);
            channel.close();
        }

        // Decode the encoded message

        bufferOffset = 0;
        MESSAGE_HEADER.reset(directBuffer, bufferOffset, messageTemplateVersion);

        // Lookup the applicable flyweight to decode this type of message based on templateId and version.
        final int templateId = MESSAGE_HEADER.templateId();
        final int actingVersion = MESSAGE_HEADER.version();
        final int actingBlockLength = MESSAGE_HEADER.blockLength();

        bufferOffset += MESSAGE_HEADER.size();
        decode(CAR, directBuffer, bufferOffset, actingBlockLength, actingVersion);
    }

    private static int encode(final Car car, final DirectBuffer directBuffer, final int bufferOffset)
    {
        final int srcOffset = 0;

        car.resetForEncode(directBuffer, bufferOffset)
           .serialNumber(1234)
           .modelYear(2013)
           .available(BooleanType.TRUE)
           .code(Model.A)
           .putVehicleCode(VEHICLE_CODE, srcOffset);

        for (int i = 0, size = car.someNumbersLength(); i < size; i++)
        {
            car.someNumbers(i, i);
        }

        car.extras()
           .cruiseControl(true)
           .sportsPack(true)
           .sunRoof(false);

        car.engine()
           .capacity(2000)
           .numCylinders((short)4)
           .putManufacturerCode(MANUFACTURER_CODE, srcOffset);

        car.fuelFiguresCount(3)
           .next().speed(30).mpg(35.9f)
           .next().speed(55).mpg(49.0f)
           .next().speed(75).mpg(40.0f);

        final Car.PerformanceFigures performanceFigures = car.performanceFiguresCount(2);
        performanceFigures.next()
            .octaneRating((short)95)
            .accelerationCount(3)
                .next().mph(30).seconds(4.0f)
                .next().mph(60).seconds(7.5f)
                .next().mph(100).seconds(12.2f);
        performanceFigures.next()
            .octaneRating((short)99)
            .accelerationCount(3)
                .next().mph(30).seconds(3.8f)
                .next().mph(60).seconds(7.1f)
                .next().mph(100).seconds(11.8f);

        car.putMake(MAKE, srcOffset, MAKE.length);
        car.putModel(MODEL, srcOffset, MODEL.length);

        return car.size();
    }

    private static void decode(final Car car,
                               final DirectBuffer directBuffer,
                               final int bufferOffset,
                               final int actingBlockLength,
                               final int actingVersion)
        throws Exception
    {
        final byte[] buffer = new byte[128];
        final StringBuilder sb = new StringBuilder();

        car.resetForDecode(directBuffer, bufferOffset, actingBlockLength, actingVersion);

        sb.append("\ncar.templateId=").append(car.templateId());
        sb.append("\ncar.serialNumber=").append(car.serialNumber());
        sb.append("\ncar.modelYear=").append(car.modelYear());
        sb.append("\ncar.available=").append(car.available());
        sb.append("\ncar.code=").append(car.code());

        sb.append("\ncar.someNumbers=");
        for (int i = 0, size = car.someNumbersLength(); i < size; i++)
        {
            sb.append(car.someNumbers(i)).append(", ");
        }

        sb.append("\ncar.vehicleCode=");
        for (int i = 0, size = car.vehicleCodeLength(); i < size; i++)
        {
            sb.append((char)car.vehicleCode(i));
        }

        final OptionalExtras extras = car.extras();
        sb.append("\ncar.extras.cruiseControl=").append(extras.cruiseControl());
        sb.append("\ncar.extras.sportsPack=").append(extras.sportsPack());
        sb.append("\ncar.extras.sunRoof=").append(extras.sunRoof());

        final Engine engine = car.engine();
        sb.append("\ncar.engine.capacity=").append(engine.capacity());
        sb.append("\ncar.engine.numCylinders=").append(engine.numCylinders());
        sb.append("\ncar.engine.maxRpm=").append(engine.maxRpm());
        sb.append("\ncar.engine.manufacturerCode=");
        for (int i = 0, size = engine.manufacturerCodeLength(); i < size; i++)
        {
            sb.append((char)engine.manufacturerCode(i));
        }

        sb.append("\ncar.engine.fuel=").append(new String(buffer, 0, engine.getFuel(buffer, 0, buffer.length), "ASCII"));

        for (final Car.FuelFigures fuelFigures : car.fuelFigures())
        {
            sb.append("\ncar.fuelFigures.speed=").append(fuelFigures.speed());
            sb.append("\ncar.fuelFigures.mpg=").append(fuelFigures.mpg());
        }

        for (final Car.PerformanceFigures performanceFigures : car.performanceFigures())
        {
            sb.append("\ncar.performanceFigures.octaneRating=").append(performanceFigures.octaneRating());

            for (final Car.PerformanceFigures.Acceleration acceleration : performanceFigures.acceleration())
            {
                sb.append("\ncar.performanceFigures.acceleration.mph=").append(acceleration.mph());
                sb.append("\ncar.performanceFigures.acceleration.seconds=").append(acceleration.seconds());
            }
        }

        sb.append("\ncar.make=").append(new String(buffer, 0, car.getMake(buffer, 0, buffer.length), car.makeCharacterEncoding()));
        sb.append("\ncar.model=").append(new String(buffer, 0, car.getModel(buffer, 0, buffer.length), car.modelCharacterEncoding()));

        sb.append("\ncar.size=").append(car.size());

        System.out.println(sb);
    }
}
