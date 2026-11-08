package com.clj.blesample;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clj.blesample.operation.OperationActivity;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import java.util.Arrays;
import java.util.Locale;

/** Local BLE transparent transmission controller for the TBOX/instrument link. */
public class VoltageControlActivity extends AppCompatActivity {
    private static final String NUS_SERVICE = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String NUS_WRITE = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String NUS_NOTIFY = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

    private BleDevice device;
    private EditText serviceUuid;
    private EditText writeUuid;
    private EditText notifyUuid;
    private EditText rawHex;
    private TextView log;
    private Spinner voltage;
    private Spinner manufacturer;
    private Spinner capacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device = getIntent().getParcelableExtra(OperationActivity.KEY_DATA);
        setTitle("TBOX本地电压配置");
        setContentView(buildView());
    }

    private View buildView() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 20, 28, 28);
        scroll.addView(root);

        TextView deviceText = text(device == null ? "未连接" : "已连接：" + device.getName() + "  " + device.getMac(), 15);
        root.addView(deviceText);

        root.addView(label("BLE透传UUID（资料中的 Nordic UART 默认值，可修改）"));
        serviceUuid = edit(NUS_SERVICE);
        writeUuid = edit(NUS_WRITE);
        notifyUuid = edit(NUS_NOTIFY);
        root.addView(serviceUuid);
        root.addView(writeUuid);
        root.addView(notifyUuid);
        Button notifyButton = button("开启接收通知");
        root.addView(notifyButton);
        notifyButton.setOnClickListener(v -> enableNotify());

        root.addView(label("电池平台参数"));
        voltage = spinner(new String[]{"48V（1）", "60V（2）", "72V（3）"});
        manufacturer = spinner(new String[]{"天能（2）", "超威（3）"});
        capacity = spinner(new String[]{"6Ah（1）", "12Ah（2）", "20Ah（3）", "23Ah（4）", "32Ah（5）", "38Ah（6）", "45Ah（7）"});
        root.addView(voltage);
        root.addView(manufacturer);
        root.addView(capacity);

        Button sendNew = button("下发新配置帧（0x24000168 / 仪表0x89）");
        root.addView(sendNew);
        sendNew.setOnClickListener(v -> sendRs485Preset());

        Button sendLegacy = button("下发旧组合帧（自动匹配1~9）");
        root.addView(sendLegacy);
        sendLegacy.setOnClickListener(v -> sendLegacyPreset());

        root.addView(label("原始十六进制透传（空格可选）"));
        rawHex = edit("");
        rawHex.setHint("例如：CF 89 40 ... ED");
        rawHex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        root.addView(rawHex);
        Button sendRaw = button("发送原始 HEX");
        root.addView(sendRaw);
        sendRaw.setOnClickListener(v -> sendRaw());

        log = text("收发日志\n", 12);
        log.setTextColor(Color.DKGRAY);
        root.addView(log);
        return scroll;
    }

    private TextView label(String value) {
        TextView view = text(value, 13);
        view.setPadding(0, 18, 0, 6);
        return view;
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        return view;
    }

    private EditText edit(String value) {
        EditText edit = new EditText(this);
        edit.setText(value);
        edit.setTextSize(12);
        edit.setSingleLine(true);
        return edit;
    }

    private Spinner spinner(String[] values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values));
        return spinner;
    }

    private Button button(String value) {
        Button button = new Button(this);
        button.setText(value);
        return button;
    }

    private void enableNotify() {
        if (device == null) return;
        BleManager.getInstance().notify(device, serviceUuid.getText().toString().trim(), notifyUuid.getText().toString().trim(), new BleNotifyCallback() {
            @Override public void onNotifySuccess() { append("通知已开启"); }
            @Override public void onNotifyFailure(BleException exception) { append("通知失败：" + exception); }
            @Override public void onCharacteristicChanged(byte[] data) { append("RX " + HexUtil.formatHexString(data, true)); }
        });
    }

    private void sendRs485Preset() {
        int v = voltage.getSelectedItemPosition() + 1;
        int m = manufacturer.getSelectedItemPosition() + 2;
        int c = capacity.getSelectedItemPosition() + 1;
        byte[] payload = new byte[64];
        Arrays.fill(payload, (byte) 0xCC);
        payload[2] = 0x20;                 // tbox_Set_Valid
        payload[7] = 0x50;                 // no legacy combination in new frame
        payload[14] = (byte) (m | (v << 4));
        payload[22] = 0x10;                // remote power request
        payload[23] = 0x70;
        payload[27] = (byte) (c << 4);
        payload[39] = (byte) 0x90;
        payload[47] = (byte) 0xA0;
        payload[55] = (byte) 0xB0;
        payload[63] = (byte) 0xC0;
        send(wrapRs485(0x89, payload), String.format(Locale.US, "新配置 v=%d m=%d c=%d", v, m, c));
    }

    private void sendLegacyPreset() {
        int v = voltage.getSelectedItemPosition() + 1;
        int m = manufacturer.getSelectedItemPosition() + 2;
        int c = capacity.getSelectedItemPosition() + 1;
        int combo = legacyCombo(v, m, c);
        if (combo == 0) {
            Toast.makeText(this, "该厂家/电压/容量没有旧指令组合", Toast.LENGTH_LONG).show();
            return;
        }
        byte[] payload = new byte[64];
        Arrays.fill(payload, (byte) 0xCC);
        payload[2] = 0x20;
        payload[7] = (byte) (0x50 + combo);
        payload[14] = (byte) (m | (v << 4));
        payload[22] = 0x10;
        payload[23] = 0x70;
        payload[27] = (byte) (c << 4);
        payload[39] = (byte) 0x90;
        payload[47] = (byte) 0xA0;
        payload[55] = (byte) 0xB0;
        payload[63] = (byte) 0xC0;
        send(wrapRs485(0x89, payload), "旧组合编号=" + combo);
    }

    private int legacyCombo(int v, int m, int c) {
        if (m == 2 && v == 1 && c == 3) return 1;
        if (m == 2 && v == 1 && c == 4) return 2;
        if (m == 2 && v == 2 && c == 3) return 3;
        if (m == 2 && v == 2 && c == 4) return 4;
        if (m == 2 && v == 3 && c == 3) return 5;
        if (m == 2 && v == 3 && c == 4) return 6;
        if (m == 3 && v == 3 && c == 4) return 7;
        if (m == 3 && v == 3 && c == 5) return 8;
        if (m == 2 && v == 1 && c == 2) return 9;
        return 0;
    }

    private byte[] wrapRs485(int address, byte[] payload) {
        byte[] frame = new byte[payload.length + 7];
        frame[0] = 'C';
        frame[1] = 'F';
        frame[2] = (byte) address;
        frame[3] = (byte) payload.length;
        System.arraycopy(payload, 0, frame, 4, payload.length);
        int sum = address + payload.length;
        for (byte b : payload) sum += b & 0xFF;
        frame[payload.length + 4] = (byte) sum;
        frame[payload.length + 5] = 'E';
        frame[payload.length + 6] = 'D';
        return frame;
    }

    private void sendRaw() {
        String value = rawHex.getText().toString().trim();
        if (value.isEmpty()) return;
        try { send(HexUtil.hexStringToBytes(value), "原始 HEX"); }
        catch (Exception e) { Toast.makeText(this, "HEX格式错误", Toast.LENGTH_LONG).show(); }
    }

    private void send(byte[] data, String description) {
        if (device == null) return;
        BleManager.getInstance().write(device, serviceUuid.getText().toString().trim(), writeUuid.getText().toString().trim(), data, new BleWriteCallback() {
            @Override public void onWriteSuccess(int current, int total, byte[] justWrite) { append("TX " + description + "\n" + HexUtil.formatHexString(justWrite, true)); }
            @Override public void onWriteFailure(BleException exception) { append("发送失败：" + exception); }
        });
    }

    private void append(String value) {
        runOnUiThread(() -> log.append(value + "\n"));
    }
}
