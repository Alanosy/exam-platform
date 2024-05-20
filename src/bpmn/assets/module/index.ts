// 翻译模块
import TranslationModule from './Translate';
import { ModuleDeclaration } from 'didi';
import CustomPaletteProvider from './Palette/CustomPaletteProvider';
import CustomRenderer from './Renderer/CustomRenderer';
import CustomContextPadProvider from './ContextPad/CustomContextPadProvider';

const Module: ModuleDeclaration[] = [
  {
    __init__: ['customPaletteProvider', 'customContextPadProvider', 'customRenderer'],
    customPaletteProvider: ['type', CustomPaletteProvider],
    customRenderer: ['type', CustomRenderer],
    customContextPadProvider: ['type', CustomContextPadProvider]
  },
  TranslationModule
];
export default Module;
