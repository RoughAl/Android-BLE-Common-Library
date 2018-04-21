package no.nordicsemi.android.ble.common.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.common.profile.RecordAccessControlPointCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("WeakerAccess")
public abstract class RecordAccessControlPointDataCallback implements ProfileDataCallback, RecordAccessControlPointCallback {
	private final static int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
	private final static int OP_CODE_RESPONSE_CODE = 6;
	private final static int OPERATOR_NULL = 0;
	private final static int SUCCESS = 1;
	private final static int SUCCESS_WITH_NO_RECORDS = 6;

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		if (data.size() < 3) {
			onInvalidDataReceived(device, data);
			return;
		}

		final int opCode = data.getIntValue(Data.FORMAT_UINT8, 0);
		if (opCode != OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE && opCode != OP_CODE_RESPONSE_CODE) {
			onInvalidDataReceived(device, data);
			return;
		}

		final int operator = data.getIntValue(Data.FORMAT_UINT8, 1);
		if (operator != OPERATOR_NULL) {
			onInvalidDataReceived(device, data);
			return;
		}

		switch (opCode) {
			case OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE: {
				// Field size is defined per service
				int numberOfRecords;

				switch (data.size() - 2) {
					case 1:
						numberOfRecords = data.getIntValue(Data.FORMAT_UINT8, 2);
						break;
					case 2:
						numberOfRecords = data.getIntValue(Data.FORMAT_UINT16, 2);
						break;
					case 4:
						numberOfRecords = data.getIntValue(Data.FORMAT_UINT32, 2);
						break;
					default:
						// Other field sizes are not supported
						onInvalidDataReceived(device, data);
						return;
				}
				onNumberOfRecordsReceived(numberOfRecords);
				break;
			}
			case OP_CODE_RESPONSE_CODE: {
				final int responseCode = data.getIntValue(Data.FORMAT_UINT8, 2);
				if (responseCode == SUCCESS) {
					onRecordAccessOperationCompleted();
				} else if (responseCode == SUCCESS_WITH_NO_RECORDS) {
					onRecordAccessOperationCompletedWithNoRecordsFound();
				} else {
					onRecordAccessOperationError(responseCode);
				}
				break;
			}
		}
	}
}
